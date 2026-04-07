package com.jiejie.rpc.core.client;

import com.jiejie.rpc.core.entity.RpcRequest;
import com.jiejie.rpc.core.registry.ServiceDiscovery;
import com.jiejie.rpc.core.registry.ZkServiceDiscovery;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;

/**
 * 基于 Netty 实现的高性能 RPC 客户端
 * 核心逻辑：服务发现 -> 建立连接 -> 发送请求 -> 等待响应
 *
 * @author JieJie
 */
public class NettyRpcClient implements RpcClient {

    private final ServiceDiscovery serviceDiscovery;
    private static final EventLoopGroup group = new NioEventLoopGroup();

    public NettyRpcClient() {
        // 默认初始化 Zookeeper 服务发现实现
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
            // 1. 根据接口名从 ZK 获取目标机器地址（实现服务去中心化）
            InetSocketAddress address = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 使用 Java 原生序列化编解码
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
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