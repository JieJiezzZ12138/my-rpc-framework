package com.jiejie.rpc.core.server;

import com.jiejie.rpc.core.provider.ServiceProvider;

/**
 * RPC 服务端通用接口。
 * * @author jiejie
 */
public interface RpcServer {
    /**
     * 启动服务
     * @param serviceProvider 服务注册中心
     * @param port 监听端口
     */
    void start(ServiceProvider serviceProvider, int port);
}