package com.jiejie.rpc.core.server;

import com.jiejie.rpc.core.provider.ServiceProvider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * 基于 Netty 实现的高性能 NIO 服务端。
 * * @author jiejie
 */
public class NettyRpcServer implements RpcServer {

    @Override
    public void start(ServiceProvider serviceProvider, int port) {
        // 用于接收连接的线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 用于处理具体 I/O 读写的线程组
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // TCP 半连接队列大小
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 开启 TCP 心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // Netty 自带的对象编解码器，简化开发（后续 V6.0 替换为自定义协议）
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            // 核心业务处理器
                            pipeline.addLast(new NettyServerHandler(serviceProvider));
                        }
                    });

            // 绑定端口并同步启动
            ChannelFuture future = serverBootstrap.bind(port).sync();
            System.out.println("【V5.0 Netty Server】已在端口 " + port + " 成功就绪");

            // 阻塞直至通道关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}