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
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

/**
 * 基于 Netty 实现的高性能 RPC 客户端 (V8.0 高性能通信与 SPI 序列化版)
 * <p>
 * 核心逻辑：服务发现 -> 节点存活校验 -> 建立连接 -> 发送请求 -> 等待响应
 * 架构更新：废弃原生 Java 序列化，引入 SPI 动态序列化引擎，并内置自定义协议编解码器解决 TCP 粘包问题。
 * </p>
 *
 * @author JieJie
 */
public class NettyRpcClient implements RpcClient {

    private final ServiceDiscovery serviceDiscovery;

    // 复用 EventLoopGroup，避免每次 RPC 调用都创建新线程池造成内存和 CPU 资源浪费
    private static final EventLoopGroup group = new NioEventLoopGroup();

    public NettyRpcClient() {
        // 在构造函数中单例初始化 Zookeeper 服务发现组件。
        // 确保底层的负载均衡器在整个客户端运行周期内保持状态记忆。
        this.serviceDiscovery = new ZkServiceDiscovery();
    }

    /**
     * 发送 RPC 请求并同步获取响应结果
     * @param rpcRequest 包含调用信息的请求实体
     * @return 远程方法执行后的结果
     */
    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        try {
            // 1. 根据接口名从 ZK 获取目标机器地址（底层会触发负载均衡算法）
            InetSocketAddress address = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());

            // 【防御性拦截】：如果在 ZK 中没找到该服务的任何可用节点，优雅抛出运行时异常
            if (address == null) {
                throw new RuntimeException("【RPC 路由失败】集群中没有任何存活的节点可供调用接口：" + rpcRequest.getInterfaceName());
            }

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();

                            // ==========================================
                            // 【V8.0 核心改造】：换装新一代高性能通信引擎
                            // ==========================================
                            // 动态获取 SPI 注入的序列化器 (如 JSON, Kryo 等)
                            Serializer serializer = SpiSerializerFactory.getSerializer();

                            // 【出站组件】：将 RpcRequest 编码成带有 4 字节长度头的二进制流发给服务端
                            pipeline.addLast(new RpcEncoder(serializer));

                            // 【入站组件】：解决 TCP 粘包/半包，并将纯净的字节流解码为对象
                            // 注意：客户端期待收回的是 Response，所以目标类必须是 RpcResponse.class
                            pipeline.addLast(new RpcDecoder(RpcResponse.class, serializer));

                            // 【入站组件】：最终的业务处理，拿到 RpcResponse 并解除主线程的阻塞
                            pipeline.addLast(new NettyClientHandler());
                        }
                    });

            // 2. 发起同步连接并等待
            ChannelFuture future = bootstrap.connect(address.getHostName(), address.getPort()).sync();
            Channel channel = future.channel();

            // 3. 发送请求报文
            channel.writeAndFlush(rpcRequest).sync();

            // 4. 阻塞等待 Channel 关闭（收到响应后 Handler 会主动 close）
            channel.closeFuture().sync();

            // 5. 从 Channel 属性中提取 Handler 暂存的执行结果
            AttributeKey<Object> key = AttributeKey.valueOf("rpcResponse");
            return channel.attr(key).get();

        } catch (InterruptedException e) {
            System.err.println("【客户端】连接或传输异常：" + e.getMessage());
            return null;
        }
    }
}