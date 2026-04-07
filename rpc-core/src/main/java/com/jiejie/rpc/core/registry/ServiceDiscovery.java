package com.jiejie.rpc.core.registry;

import java.net.InetSocketAddress;

/**
 * 服务发现接口：根据服务名从 ZK 获取一个可用的地址。
 */
public interface ServiceDiscovery {
    InetSocketAddress lookupService(String serviceName);
}