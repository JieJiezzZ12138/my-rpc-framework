package com.jiejie.rpc.core.provider;

import java.util.Set;

/**
 * 服务注册表接口
 */
public interface ServiceProvider {
    /**
     * 保存服务实例
     */
    <T> void register(T service, Class<T> serviceClass);

    /**
     * 获取服务实例
     */
    Object getService(String serviceName);

    /**
     * 【V6.0 新增】获取当前所有已注册的服务名
     * 用于在服务端启动时，批量将服务同步到 Zookeeper
     */
    Set<String> getAllServiceNames();
}