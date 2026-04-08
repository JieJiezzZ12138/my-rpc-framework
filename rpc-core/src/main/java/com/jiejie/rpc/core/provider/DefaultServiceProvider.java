package com.jiejie.rpc.core.provider;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的服务注册表实现 (V11.0 核心组件)
 * <p>
 * 职责：本地容器。负责在内存中存储接口名与对应的实现类 Bean 实例。
 * </p>
 *
 * @author JieJie
 */
public class DefaultServiceProvider implements ServiceProvider {

    /**
     * 本地服务映射表
     * Key: 接口的全限定类名 (String)
     * Value: 对应的服务实现类 Bean 实例 (Object)
     */
    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    @Override
    public <T> void register(T service, Class<T> serviceClass) {
        String serviceName = serviceClass.getCanonicalName();
        if (serviceMap.containsKey(serviceName)) {
            return;
        }
        serviceMap.put(serviceName, service);
        System.out.println("【本地容器】成功挂载服务: " + serviceName);
    }

    @Override
    public Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RuntimeException("【本地容器异常】未找到该服务的实现类: " + serviceName);
        }
        return service;
    }

    @Override
    public Set<String> getAllServiceNames() {
        return serviceMap.keySet();
    }
}