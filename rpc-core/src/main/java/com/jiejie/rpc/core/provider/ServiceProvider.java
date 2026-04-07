package com.jiejie.rpc.core.provider;

import java.util.Set;

/**
 * 本地服务注册表接口
 * 负责管理服务端本地已实现的业务对象，并作为同步源将服务元数据推送至远程注册中心（如 Zookeeper）
 *
 * @author JieJie
 * @date 2026-04-07
 */
public interface ServiceProvider {

    /**
     * 向本地注册表中挂载服务实例
     * * @param service      具体的业务实现类对象（单例）
     * @param serviceClass 该实现类所对应的接口类型，用于确定唯一的服务名
     * @param <T>          泛型标记，确保传入对象与接口类型一致
     */
    <T> void register(T service, Class<T> serviceClass);

    /**
     * 根据接口名称从本地容器中检索对应的服务实例
     * * @param serviceName 接口的全限定类名
     * @return 返回可执行的方法调用对象（实现类实例）
     */
    Object getService(String serviceName);

    /**
     * 【V6.0 新增】获取当前节点所有已注册的服务接口名称
     * 核心用途：服务端启动时，利用此集合将所有本地服务批量同步至分布式注册中心（如 Zookeeper）
     * * @return 包含所有接口全限定名的集合
     */
    Set<String> getAllServiceNames();
}