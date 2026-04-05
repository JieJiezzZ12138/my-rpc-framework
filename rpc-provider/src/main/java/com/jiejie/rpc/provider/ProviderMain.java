package com.jiejie.rpc.provider;

import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.api.PingService;
import com.jiejie.rpc.core.provider.ServiceProvider;
import com.jiejie.rpc.core.provider.ServiceProviderImpl;
import com.jiejie.rpc.core.server.RpcServer;

/**
 * 服务提供方启动入口。
 * 演示多服务注册与容器化启动流程。
 * * @author jiejie
 */
public class ProviderMain {
    public static void main(String[] args) {
        // 1. 初始化服务注册表
        ServiceProvider serviceProvider = new ServiceProviderImpl();

        // 2. 注册多个不同的业务实现类
        serviceProvider.register(new HelloServiceImpl(), HelloService.class);
        serviceProvider.register(new PingServiceImpl(), PingService.class);

        // 3. 启动通用 RPC 容器
        RpcServer rpcServer = new RpcServer();
        rpcServer.start(serviceProvider, 9000);
    }
}