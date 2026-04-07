package com.jiejie.rpc.core.provider;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地服务注册表实现类
 * 负责在服务端内存中缓存业务实现类的实例，供 RPC 引擎在接收到请求时进行反射调用
 *
 * @author JieJie
 * @date 2026-04-07
 */
public class ServiceProviderImpl implements ServiceProvider {

    /**
     * 本地服务映射表
     * Key:   接口的全限定名 (Canonical Name)
     * Value: 对应的业务实现类单例对象
     * 使用 ConcurrentHashMap 确保在高并发注册或查询服务时的线程安全性
     */
    private final ConcurrentHashMap<String, Object> serviceMap = new ConcurrentHashMap<>();

    /**
     * 将服务实例注册到本地缓存中
     *
     * @param service      接口的具体实现类对象
     * @param serviceClass 接口的 Class 对象，用于提取唯一服务名
     * @param <T>          业务接口类型
     */
    @Override
    public <T> void register(T service, Class<T> serviceClass) {
        String serviceName = serviceClass.getCanonicalName();
        // 如果该接口已注册过，则跳过，避免重复覆盖
        if (serviceMap.containsKey(serviceName)) return;

        serviceMap.put(serviceName, service);
        System.out.println("【本地注册】接口 [" + serviceName + "] 已成功挂载至服务端容器");
    }

    /**
     * 根据接口名称获取本地持有的业务实现对象
     *
     * @param serviceName 接口全限定名
     * @return 对应的实现类对象
     * @throws RuntimeException 当本地容器未找到对应服务时抛出异常
     */
    @Override
    public Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RuntimeException("【容器错误】本地未找到指定服务：" + serviceName);
        }
        return service;
    }

    /**
     * 获取当前节点注册的所有服务接口名
     * 核心用途：在 V6.0 启动流程中，用于遍历本地所有服务并同步至分布式注册中心（如 Zookeeper）
     *
     * @return 接口名称的 Set 集合
     */
    @Override
    public Set<String> getAllServiceNames() {
        // 返回 ConcurrentHashMap 的 Key 集合
        return serviceMap.keySet();
    }
}