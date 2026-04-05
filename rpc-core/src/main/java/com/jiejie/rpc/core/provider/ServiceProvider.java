package com.jiejie.rpc.core.provider;

/**
 * 本地服务注册表接口。
 * 定义了服务端服务实例的存取规范，是实现多服务调用的核心组件。
 * * @author jiejie
 */
public interface ServiceProvider {
    /**
     * 将服务实例及其接口类型注册进容器
     * @param service 具体的实现类对象
     * @param serviceClass 服务对应的接口类型
     */
    <T> void register(T service, Class<T> serviceClass);

    /**
     * 根据接口名称获取对应的服务实现类实例
     * @param serviceName 接口的全限定名
     * @return 匹配的服务实例
     */
    Object getService(String serviceName);
}