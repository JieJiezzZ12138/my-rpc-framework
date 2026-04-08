package com.jiejie.rpc.core.server;

/**
 * RPC 服务端通用接口 (V11.0 架构升级版)
 * <p>
 * 职责：定义服务端引擎的启动规范。
 * 在 Spring 自动化版本中，相关的服务注册表与端口配置已通过构造函数或注入完成，
 * 启动方法不再需要显式传递业务参数。
 * </p>
 *
 * @author JieJie
 */
public interface RpcServer {

    /**
     * 启动 RPC 服务端引擎
     * <p>
     * 该方法将执行以下操作：
     * 1. 扫描并同步本地服务至注册中心（如 Zookeeper）。
     * 2. 初始化 Netty 线程模型并绑定监听端口。
     * 3. 开启心跳监测与数据传输流水线。
     * </p>
     */
    void start();
}