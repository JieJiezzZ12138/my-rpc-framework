package com.jiejie.rpc.core.provider;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务注册表实现类
 */
public class ServiceProviderImpl implements ServiceProvider {

    // Key: 接口全限定名, Value: 对应的实现类对象
    private final ConcurrentHashMap<String, Object> serviceMap = new ConcurrentHashMap<>();

    @Override
    public <T> void register(T service, Class<T> serviceClass) {
        String serviceName = serviceClass.getCanonicalName();
        if (serviceMap.containsKey(serviceName)) return;
        serviceMap.put(serviceName, service);
        System.out.println("【服务注册】接口 [" + serviceName + "] 已成功挂载至本地注册表");
    }

    @Override
    public Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RuntimeException("本地未找到服务：" + serviceName);
        }
        return service;
    }

    @Override
    public Set<String> getAllServiceNames() {
        // 直接返回 Map 中所有的 Key (即接口名集合)
        return serviceMap.keySet();
    }
}