package com.jiejie.rpc.provider;

import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.api.PingService;
import com.jiejie.rpc.core.provider.ServiceProvider;
import com.jiejie.rpc.core.provider.ServiceProviderImpl;
import com.jiejie.rpc.core.server.NettyRpcServer;
import com.jiejie.rpc.core.server.RpcServer;

/**
 * V6.0 服务提供方启动入口
 */
public class ProviderMain {
    public static void main(String[] args) {
        // 1. 初始化本地服务注册表
        ServiceProvider serviceProvider = new ServiceProviderImpl();

        // 2. 注册业务实现类
        serviceProvider.register(new HelloServiceImpl(), HelloService.class);
        serviceProvider.register(new PingServiceImpl(), PingService.class);

        // 3. 启动 Netty 服务端
        // 【核心修复】：传入当前服务器的 IP 和 端口
        // 这里的 IP 会被注册到 Zookeeper，供客户端查询
        RpcServer rpcServer = new NettyRpcServer("127.0.0.1", 9000);

        // 启动容器
        rpcServer.start(serviceProvider, 9000);
    }
}