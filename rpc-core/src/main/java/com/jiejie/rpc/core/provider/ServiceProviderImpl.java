package com.jiejie.rpc.core.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务注册中心的默认实现。
 * 采用 ConcurrentHashMap 维护接口名与服务单例的映射，保障高并发场景下的线程安全。
 * * @author jiejie
 */
public class ServiceProviderImpl implements ServiceProvider {

    /** 缓存容器：Key 为接口的 CanonicalName，Value 为服务实现类对象 */
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    @Override
    public <T> void register(T service, Class<T> serviceClass) {
        String serviceName = serviceClass.getCanonicalName();
        if (serviceMap.containsKey(serviceName)) return;
        serviceMap.put(serviceName, service);
        System.out.println("【服务注册】接口 [" + serviceName + "] 已成功挂载至注册中心");
    }

    @Override
    public Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RuntimeException("【RPC 异常】未能在注册中心找到名为 [" + serviceName + "] 的服务实例");
        }
        return service;
    }
}