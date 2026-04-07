package com.jiejie.rpc.provider;

import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.api.PingService;
import com.jiejie.rpc.core.provider.ServiceProvider;
import com.jiejie.rpc.core.provider.ServiceProviderImpl;
import com.jiejie.rpc.core.server.NettyRpcServer;
import com.jiejie.rpc.core.server.RpcServer;

/**
 * RPC 服务提供方启动入口 (V6.0 分布式版本)
 * 职责：初始化业务实现类，将其加载至本地容器，并启动网络引擎完成服务发布
 *
 * @author JieJie
 * @date 2026-04-07
 */
public class ProviderMain {
    public static void main(String[] args) {
        // 1. 初始化本地服务注册表（容器）
        // 用于存放接口名与具体实现类实例的映射关系，供后续反射调用使用
        ServiceProvider serviceProvider = new ServiceProviderImpl();

        // 2. 将具体的业务实现类手动注册到本地容器中
        // 只有注册到此处的服务，才能被远程客户端通过 RPC 调用访问
        serviceProvider.register(new HelloServiceImpl(), HelloService.class);
        serviceProvider.register(new PingServiceImpl(), PingService.class);

        // 3. 实例化 Netty 服务端引擎
        // 【核心说明】：此处传入当前服务器的 IP 和 端口
        // 该地址信息会被同步注册到 Zookeeper 中心，作为客户端“问路”时的返回结果
        RpcServer rpcServer = new NettyRpcServer("127.0.0.1", 9000);

        // 4. 启动服务端网络容器
        // 开启 Netty 端口监听，并触发自动化的 Zookeeper 服务上报流程
        rpcServer.start(serviceProvider, 9000);
    }
}