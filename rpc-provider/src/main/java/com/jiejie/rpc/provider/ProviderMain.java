package com.jiejie.rpc.provider;

import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.api.PingService;
import com.jiejie.rpc.core.provider.ServiceProvider;
import com.jiejie.rpc.core.provider.ServiceProviderImpl;
import com.jiejie.rpc.core.server.NettyRpcServer;
import com.jiejie.rpc.core.server.RpcServer;

/**
 * 服务提供方启动入口 (V5.0 Netty 重构版)。
 * 演示多服务注册与 Netty NIO 容器的高性能启动流程。
 * * @author jiejie
 */
public class ProviderMain {
    public static void main(String[] args) {
        // 1. 初始化本地服务注册中心
        ServiceProvider serviceProvider = new ServiceProviderImpl();

        // 2. 挂载业务实现类至注册中心 (路由注册)
        serviceProvider.register(new HelloServiceImpl(), HelloService.class);
        serviceProvider.register(new PingServiceImpl(), PingService.class);

        // 3. 启动通用 RPC 容器
        // 【核心修复】：RpcServer 已经是接口，这里必须实例化它的 Netty 实现类
        RpcServer rpcServer = new NettyRpcServer();

        // 启动服务端，注入注册表并绑定端口
        rpcServer.start(serviceProvider, 9000);
    }
}