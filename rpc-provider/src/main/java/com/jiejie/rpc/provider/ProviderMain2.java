package com.jiejie.rpc.provider;

import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.api.PingService;
import com.jiejie.rpc.core.provider.ServiceProvider;
import com.jiejie.rpc.core.provider.ServiceProviderImpl;
import com.jiejie.rpc.core.server.NettyRpcServer;
import com.jiejie.rpc.core.server.RpcServer;

/**
 * RPC 服务提供方启动入口 (V6.5 负载均衡集群版 - 节点 A)
 * <p>
 * 职责：初始化业务实现类，将其加载至本地容器，并启动网络引擎完成服务发布。
 * 架构说明：在 V6.5 中，本节点将与另外的节点（如 9001 端口）共同组成提供者集群，
 * 共同分担来自 Consumer 客户端的并发请求压力。
 * </p>
 *
 * @author JieJie
 * @date 2026-04-08
 */
public class ProviderMain2 {

    // 【V6.5 集群配置】：当前节点的监听端口
    private static final int PORT = 9001;

    public static void main(String[] args) {
        // 1. 初始化本地服务注册表（容器）
        ServiceProvider serviceProvider = new ServiceProviderImpl();

        // 2. 注册业务实现类到本地容器
        serviceProvider.register(new HelloServiceImpl(), HelloService.class);
        serviceProvider.register(new PingServiceImpl(), PingService.class);

        System.out.println("【节点初始化】本地业务服务已成功加载。");

        // 3. 实例化 Netty 服务端引擎
        // 传入当前服务器的 IP 和 端口。在 ZK 树上，它会表现为一个临时节点 (Ephemeral Node)
        // 例如：/my-rpc/com.jiejie.rpc.api.HelloService/127.0.0.1:9000
        RpcServer rpcServer = new NettyRpcServer("127.0.0.1", PORT);

        // 4. 启动服务端网络容器，正式接入集群
        System.out.println("【节点启动】正在将当前节点注册至 Zookeeper 注册中心...");
        rpcServer.start(serviceProvider, PORT);
    }
}