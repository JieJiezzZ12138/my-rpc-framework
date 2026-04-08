package com.jiejie.rpc.core.server;

import com.jiejie.rpc.core.codec.RpcDecoder;
import com.jiejie.rpc.core.codec.RpcEncoder;
import com.jiejie.rpc.core.entity.RpcRequest;
import com.jiejie.rpc.core.provider.ServiceProvider;
import com.jiejie.rpc.core.registry.ServiceRegistry;
import com.jiejie.rpc.core.registry.ZkServiceRegistry;
import com.jiejie.rpc.core.serialize.Serializer;
import com.jiejie.rpc.core.serialize.SpiSerializerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * 基于 Netty 实现的高性能 RPC 服务端引擎 (V8.0 高性能通信与 SPI 序列化版)
 * <p>
 * 职责：启动网络监听服务，并将本地服务自动同步至 Zookeeper 注册中心。
 * 架构更新：废弃原生 Java 序列化，引入 SPI 动态序列化引擎，并内置自定义协议编解码器解决 TCP 粘包问题。
 * </p>
 *
 * @author JieJie
 * @date 2026-04-08
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
        // 1. 服务同步
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

                            // 【V8.0 核心改造】：动态获取 SPI 注入的序列化器
                            Serializer serializer = SpiSerializerFactory.getSerializer();

                            // 【入站组件】：解决 TCP 粘包/半包，并将纯净的字节流解码为对象
                            // 注意：服务端接收的是客户端发来的 Request，所以目标反序列化类是 RpcRequest.class！
                            pipeline.addLast(new RpcDecoder(RpcRequest.class, serializer));

                            // 【出站组件】：将业务执行结果 RpcResponse 编码成带有 4 字节长度头的二进制流返回给客户端
                            pipeline.addLast(new RpcEncoder(serializer));

                            // 【入站后置组件】：获取到安全的 RpcRequest 后，交由业务处理器反射调用本地方法
                            pipeline.addLast(new NettyServerHandler(serviceProvider));
                        }
                    });

            // 4. 绑定端口并同步启动
            ChannelFuture future = serverBootstrap.bind(port).sync();
            System.out.println("【RPC 服务端】V8.0 引擎已就绪，正在端口 " + port + " 监听请求...");

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