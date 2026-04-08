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

/**
 * Netty RPC 服务端引擎 (V13.0 异步多路复用版)
 * <p>
 * 核心特性：
 * 1. 【协议升级】采用 24 字节固定 Header，支持 RequestId 异步匹配。
 * 2. 【多路复用】支持单条 TCP 连接上的并发请求处理。
 * 3. 【高可用】集成心跳检测与 SPI 序列化机制。
 * </p>
 *
 * @author jiejie
 */
public class NettyRpcServer implements RpcServer {

    private final int port;
    private final ServiceProvider serviceProvider;

    public NettyRpcServer(String host, int port, ServiceProvider serviceProvider) {
        this.port = port;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void start() {
        // 创建主从反应堆模型
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

                            // 1. 服务端心跳监测：30秒未收到数据则断开
                            p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));

                            // 2. SPI 获取序列化器
                            Serializer serializer = SpiSerializerFactory.getSerializer();

                            // 3. 【V13.0 升级】私有协议解码器：解析 24 字节 Header
                            p.addLast(new RpcDecoder(RpcRequest.class, serializer));

                            // 4. 【V13.0 升级】私有协议编码器：封装带 RequestId 的响应包
                            p.addLast(new RpcEncoder(serializer));

                            // 5. 业务处理器：执行反射调用并回传 RequestId
                            p.addLast(new NettyServerHandler(serviceProvider));
                        }
                    });

            System.out.println("===================================================");
            System.out.println("【JRPC 引擎】V13.0 异步多路复用版初始化成功");
            System.out.println("【核心参数】魔数: jrpc | 协议头长度: 24 字节 (含 RequestId)");
            System.out.println("【绑定地址】正在监听端口: " + port);
            System.out.println("===================================================");

            // 绑定端口并同步等待成功
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            System.err.println("【RPC 服务端】运行异常：" + e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}