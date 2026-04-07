package com.jiejie.rpc.core.registry;

import java.net.InetSocketAddress;

/**
 * 服务注册接口：将服务名与地址关联并存入 ZK。
 */
public interface ServiceRegistry {
    void register(String serviceName, InetSocketAddress inetSocketAddress);
}