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
 * 基于 Netty 实现的高性能 RPC 客户端 (V6.5 负载均衡与防御性编程版)
 * 核心逻辑：服务发现 -> 节点存活校验 -> 建立连接 -> 发送请求 -> 等待响应
 *
 * @author JieJie
 * @date 2026-04-08
 */
public class NettyRpcClient implements RpcClient {

    private final ServiceDiscovery serviceDiscovery;

    // 复用 EventLoopGroup，避免每次 RPC 调用都创建新线程池造成内存和 CPU 资源浪费
    private static final EventLoopGroup group = new NioEventLoopGroup();

    public NettyRpcClient() {
        // 【V6.5 核心架构要求】：在构造函数中单例初始化 Zookeeper 服务发现组件。
        // 这一步至关重要，它确保了底层的负载均衡器（如轮询算法中的 AtomicInteger）
        // 在整个客户端运行周期内保持状态记忆，不会每次请求都从 0 开始。
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

            // 【V6.5 核心防御补丁】：如果在 ZK 中没找到该服务的任何可用节点，优雅抛出运行时异常，
            // 直接中断后续流程，防止下方的 address.getHostName() 触发惨烈的 NullPointerException。
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
                            // 使用 Java 原生序列化编解码 (后续可作为优化点替换为 Kryo/Protobuf)
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