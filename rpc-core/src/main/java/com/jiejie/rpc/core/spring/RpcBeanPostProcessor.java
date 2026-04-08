package com.jiejie.rpc.core.spring;

import com.jiejie.rpc.core.annotation.RpcReference;
import com.jiejie.rpc.core.annotation.RpcService;
import com.jiejie.rpc.core.client.RpcClient;
import com.jiejie.rpc.core.client.RpcClientProxy;
import com.jiejie.rpc.core.provider.ServiceProvider;
import com.jiejie.rpc.core.registry.ServiceRegistry;
import com.jiejie.rpc.core.registry.ZkServiceRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RpcBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcClient rpcClient;
    private final ServiceRegistry serviceRegistry;

    // 幂等性检查：记录本进程已注册的服务，防止 Spring 重复初始化导致的 NodeExists 报错
    private final Set<String> registeredServices = ConcurrentHashMap.newKeySet();

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 9000;

    public RpcBeanPostProcessor(ServiceProvider serviceProvider, RpcClient rpcClient) {
        this.serviceProvider = serviceProvider;
        this.rpcClient = rpcClient;
        this.serviceRegistry = new ZkServiceRegistry();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            Class<?> interfaceClass = rpcService.interfaceClass();
            if (interfaceClass == Object.class) {
                interfaceClass = bean.getClass().getInterfaces()[0];
            }

            String serviceName = interfaceClass.getCanonicalName();

            // 只有当该服务未被注册过时，才执行注册逻辑
            if (!registeredServices.contains(serviceName)) {
                // 1. 注册到本地容器
                serviceProvider.register(bean, (Class) interfaceClass);
                // 2. 注册到 Zookeeper
                serviceRegistry.register(serviceName, new InetSocketAddress(HOST, PORT));

                registeredServices.add(serviceName);
                System.out.println("【V11.0 自动化】已成功暴露服务: " + interfaceClass.getSimpleName());
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(RpcReference.class)) {
                RpcClientProxy proxy = new RpcClientProxy(rpcClient);
                Object proxyInstance = proxy.getProxy(field.getType());
                field.setAccessible(true);
                try {
                    field.set(bean, proxyInstance);
                    System.out.println("【V11.0 自动化】已为 " + beanName + " 注入代理对象: " + field.getName());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}