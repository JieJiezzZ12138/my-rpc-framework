package com.jiejie.rpc.core.client;

import com.jiejie.rpc.core.codec.RpcDecoder;
import com.jiejie.rpc.core.codec.RpcEncoder;
import com.jiejie.rpc.core.entity.RpcRequest;
import com.jiejie.rpc.core.entity.RpcResponse;
import com.jiejie.rpc.core.registry.ServiceDiscovery;
import com.jiejie.rpc.core.registry.ZkServiceDiscovery;
import com.jiejie.rpc.core.serialize.Serializer;
import com.jiejie.rpc.core.serialize.SpiSerializerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Netty 实现的高性能 RPC 客户端 (V13.0 异步多路复用版)
 * <p>
 * 核心升级：
 * 1. 采用 CompletableFuture 替代传统的同步阻塞，支撑高并发异步调用。
 * 2. 移除 channel.closeFuture().sync()，为后续【长连接池化】打下基础。
 * 3. 配合 24 字节 Header 中的 requestId 实现响应精准匹配。
 * </p>
 */
@Component
public class NettyRpcClient implements RpcClient {

    private final ServiceDiscovery serviceDiscovery;
    private static final EventLoopGroup group = new NioEventLoopGroup();
    private static final Bootstrap bootstrap = new Bootstrap();

    /**
     * 【V13.0 核心】待处理请求集合
     * Key: requestId, Value: 用于存放结果的异步容器
     */
    public static final Map<Long, CompletableFuture<RpcResponse>> UNPROCESSED_REQUESTS = new ConcurrentHashMap<>();

    static {
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
    }

    public NettyRpcClient() {
        this.serviceDiscovery = new ZkServiceDiscovery();
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        // 1. 实例化异步结果容器
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        // 将请求 ID 与容器绑定，存入全局 Map，等待 NettyClientHandler 唤醒
        UNPROCESSED_REQUESTS.put(rpcRequest.getRequestId(), resultFuture);

        try {
            InetSocketAddress address = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            if (address == null) {
                UNPROCESSED_REQUESTS.remove(rpcRequest.getRequestId());
                throw new RuntimeException("【RPC 路由失败】未找到服务节点：" + rpcRequest.getInterfaceName());
            }

            // 2. 配置 Pipeline
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new IdleStateHandler(0, 10, 0, TimeUnit.SECONDS));

                    Serializer serializer = SpiSerializerFactory.getSerializer();
                    // V13.0：24 字节 Header 编解码器
                    pipeline.addLast(new RpcEncoder(serializer));
                    pipeline.addLast(new RpcDecoder(RpcResponse.class, serializer));
                    pipeline.addLast(new NettyClientHandler());
                }
            });

            // 3. 异步建立连接
            ChannelFuture future = bootstrap.connect(address).sync();
            Channel channel = future.channel();

            if (channel.isActive()) {
                // 4. 发送请求，发送完立即退出，不阻塞 IO 线程
                channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) f -> {
                    if (f.isSuccess()) {
                        System.out.println("【客户端】请求发送成功，ID: " + rpcRequest.getRequestId());
                    } else {
                        f.channel().close();
                        resultFuture.completeExceptionally(f.cause());
                        System.err.println("【客户端】发送请求失败: " + f.cause());
                    }
                });

                // 5. 【关键】同步阻塞直到 resultFuture 被 NettyClientHandler 填入结果
                // 设置 5 秒超时，防止服务端宕机导致客户端永久挂起
                RpcResponse rpcResponse = resultFuture.get(5, TimeUnit.SECONDS);

                // 拿到结果后，检查业务状态
                if (rpcResponse == null || rpcResponse.getCode() != 200) {
                    throw new RuntimeException("【RPC 调用异常】" + (rpcResponse != null ? rpcResponse.getMessage() : "无响应"));
                }
                return rpcResponse.getData();

            } else {
                throw new RuntimeException("【客户端】连接不可用");
            }

        } catch (Exception e) {
            UNPROCESSED_REQUESTS.remove(rpcRequest.getRequestId());
            Thread.currentThread().interrupt();
            throw new RuntimeException("【客户端】运行期异常", e);
        }
    }
}