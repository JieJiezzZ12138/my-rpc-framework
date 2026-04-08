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
import io.netty.util.AttributeKey;
import org.springframework.stereotype.Component; // 引入 Spring 注解

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Netty 实现的高性能 RPC 客户端 (V11.0 Spring 自动化版)
 */
@Component // 👈 补上这个，让 RpcBeanPostProcessor 的构造函数能自动注入它
public class NettyRpcClient implements RpcClient {

    private final ServiceDiscovery serviceDiscovery;
    private static final EventLoopGroup group = new NioEventLoopGroup();
    private static final Bootstrap bootstrap = new Bootstrap();

    static {
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true);
    }

    public NettyRpcClient() {
        this.serviceDiscovery = new ZkServiceDiscovery();
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        try {
            InetSocketAddress address = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            if (address == null) {
                throw new RuntimeException("【RPC 路由失败】未找到服务节点：" + rpcRequest.getInterfaceName());
            }

            // 💡 优化点：在工业级实现中，我们会用 Map<InetSocketAddress, Channel> 缓存连接
            // 现在的逻辑是每次请求新连，心跳虽然加了，但只有在连接期间有效。
            // 既然我们要一口气把 9.0 和 10.0 做了，目前的结构足以支撑 Spring 自动注入。
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    // V9.0 心跳：10秒写空闲
                    pipeline.addLast(new IdleStateHandler(0, 10, 0, TimeUnit.SECONDS));
                    Serializer serializer = SpiSerializerFactory.getSerializer();
                    pipeline.addLast(new RpcEncoder(serializer));
                    pipeline.addLast(new RpcDecoder(RpcResponse.class, serializer));
                    pipeline.addLast(new NettyClientHandler());
                }
            });

            ChannelFuture future = bootstrap.connect(address.getHostName(), address.getPort()).sync();
            Channel channel = future.channel();

            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(rpcRequest);
                channel.closeFuture().sync();
                AttributeKey<Object> key = AttributeKey.valueOf("rpcResponse");
                return channel.attr(key).get();
            } else {
                throw new RuntimeException("【客户端】连接已失效");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}