package com.jiejie.rpc.core.server;

import com.jiejie.rpc.core.codec.RpcDecoder;
import com.jiejie.rpc.core.codec.RpcEncoder;
import com.jiejie.rpc.core.entity.RpcRequest;
import com.jiejie.rpc.core.provider.ServiceProvider;
import com.jiejie.rpc.core.serialize.Serializer;
import com.jiejie.rpc.core.serialize.SpiSerializerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class NettyRpcServer implements RpcServer {
    private final int port;
    private final ServiceProvider serviceProvider;

    public NettyRpcServer(String host, int port, ServiceProvider serviceProvider) {
        this.port = port;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void start() {
        // V11.0：此处不再进行 ZK 注册，全部交给 BeanPostProcessor 自动处理
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            // 服务端心跳监测：30秒未收到客户端数据则断开
                            p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            Serializer serializer = SpiSerializerFactory.getSerializer();
                            p.addLast(new RpcDecoder(RpcRequest.class, serializer));
                            p.addLast(new RpcEncoder(serializer));
                            p.addLast(new NettyServerHandler(serviceProvider));
                        }
                    });

            System.out.println("【RPC 服务端】Netty 引擎准备就绪，正在绑定端口: " + port);
            ChannelFuture f = b.bind(port).sync();
            System.out.println("【RPC 服务端】V11.0 运行中...");
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}