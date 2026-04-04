package com.jiejie.rpc.provider;

import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.core.server.RpcServer;

/**
 * 服务端启动入口
 */
public class ProviderMain {
    public static void main(String[] args) {
        // 1. 实例化真正提供业务逻辑的服务对象
        HelloService helloService = new HelloServiceImpl();

        // 2. 实例化 RPC 通用服务端
        RpcServer rpcServer = new RpcServer();

        // 3. 把服务对象交给框架，并在 9000 端口暴露出去
        rpcServer.start(helloService, 9000);
    }
}