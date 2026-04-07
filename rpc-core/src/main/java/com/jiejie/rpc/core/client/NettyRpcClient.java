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
 * V6.0：具备服务发现能力的 Netty 客户端
 */
public class NettyRpcClient implements RpcClient {

    private final ServiceDiscovery serviceDiscovery;
    private static final EventLoopGroup group = new NioEventLoopGroup();

    public NettyRpcClient() {
        // 初始化 ZK 发现中心实现
        this.serviceDiscovery = new ZkServiceDiscovery();
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        try {
            // 1. 【V6.0 核心】：问路。根据接口名从 ZK 获取服务端的 IP 和端口
            InetSocketAddress address = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ObjectEncoder());
                            ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            ch.pipeline().addLast(new NettyClientHandler());
                        }
                    });

            // 2. 使用查到的地址进行连接
            ChannelFuture future = bootstrap.connect(address.getHostName(), address.getPort()).sync();
            Channel channel = future.channel();

            channel.writeAndFlush(rpcRequest).sync();
            channel.closeFuture().sync();

            AttributeKey<Object> key = AttributeKey.valueOf("rpcResponse");
            return channel.attr(key).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}