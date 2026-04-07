package com.jiejie.rpc.core.server;

import com.jiejie.rpc.core.provider.ServiceProvider;
import com.jiejie.rpc.core.registry.ServiceRegistry;
import com.jiejie.rpc.core.registry.ZkServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;

/**
 * V6.0：集成 Zookeeper 注册中心的 Netty 服务端
 */
public class NettyRpcServer implements RpcServer {

    private final String host;
    private final int port;
    private final ServiceRegistry serviceRegistry;

    public NettyRpcServer(String host, int port) {
        this.host = host;
        this.port = port;
        // 初始化 ZK 注册中心实现
        this.serviceRegistry = new ZkServiceRegistry();
    }

    @Override
    public void start(ServiceProvider serviceProvider, int port) {
        // 1. 【V6.0 关键】：将本地服务同步到 Zookeeper 云端
        for (String serviceName : serviceProvider.getAllServiceNames()) {
            serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(new NettyServerHandler(serviceProvider));
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(port).sync();
            System.out.println("【V6.0】RPC 服务端已在端口 " + port + " 启动并完成 ZK 注册");
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}