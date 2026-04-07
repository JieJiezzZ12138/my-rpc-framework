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
 * 基于 Netty 实现的高性能 RPC 服务端引擎 (V6.0 分布式版)
 * 职责：启动网络监听服务，并将本地服务自动同步至 Zookeeper 注册中心
 *
 * @author JieJie
 * @date 2026-04-07
 */
public class NettyRpcServer implements RpcServer {

    /** 服务端绑定的 IP 地址 */
    private final String host;
    /** 服务端监听的端口号 */
    private final int port;
    /** 分布式服务注册中心 */
    private final ServiceRegistry serviceRegistry;

    /**
     * 初始化服务端配置
     * @param host 供客户端访问的本地 IP
     * @param port 监听端口
     */
    public NettyRpcServer(String host, int port) {
        this.host = host;
        this.port = port;
        // 默认使用 Zookeeper 作为分布式注册中心实现
        this.serviceRegistry = new ZkServiceRegistry();
    }

    /**
     * 核心启动方法：执行服务同步注册并开启 Netty 监听
     * @param serviceProvider 本地服务注册表（存放实现类实例）
     * @param port 端口
     */
    @Override
    public void start(ServiceProvider serviceProvider, int port) {
        // 1. 【V6.0 核心逻辑】：服务同步
        // 遍历本地容器中的所有接口名，将其批量注册到 Zookeeper 的临时节点上
        for (String serviceName : serviceProvider.getAllServiceNames()) {
            serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
        }

        // 2. 配置 Netty 线程模型
        // bossGroup 负责接收客户端的 TCP 连接请求
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // workerGroup 负责处理已建立连接的数据读写及业务逻辑
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 3. 服务端辅助启动类配置
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 指定使用 NIO 传输通道
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加对象编码器：将返回结果序列化为二进制流发送
                            pipeline.addLast(new ObjectEncoder());
                            // 添加对象解码器：将接收到的二进制流还原为 RpcRequest 对象
                            pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            // 添加自定义业务处理器：负责反射调用本地方法
                            pipeline.addLast(new NettyServerHandler(serviceProvider));
                        }
                    });

            // 4. 绑定端口并同步启动
            ChannelFuture future = serverBootstrap.bind(port).sync();
            System.out.println("【RPC 服务端】V6.0 引擎已就绪，正在端口 " + port + " 监听请求...");

            // 5. 阻塞直至频道关闭（等待服务端正常停止）
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            System.err.println("【RPC 服务端异常】Netty 运行时被中断：" + e.getMessage());
        } finally {
            // 6. 优雅关闭线程池，释放资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}