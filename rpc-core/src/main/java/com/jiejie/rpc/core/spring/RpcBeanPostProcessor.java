package com.jiejie.rpc.core.spring;

import com.jiejie.rpc.core.annotation.RpcReference;
import com.jiejie.rpc.core.annotation.RpcService;
import com.jiejie.rpc.core.client.RpcClient;
import com.jiejie.rpc.core.client.RpcClientProxy;
import com.jiejie.rpc.core.provider.ServiceProvider;
import com.jiejie.rpc.core.registry.ServiceRegistry;
import com.jiejie.rpc.core.registry.ZkServiceRegistry;
import com.jiejie.rpc.core.util.CuratorUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RpcBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcClient rpcClient;
    private final ServiceRegistry serviceRegistry;

    // 核心：通过 @Value 自动读取 rpc.properties 里的配置
    // 如果没有配置，则默认使用公网 IP 和 9000 端口
    @Value("${rpc.zookeeper.address:39.107.74.108:2181}")
    private String zkAddress;

    @Value("${rpc.server.host:39.107.74.108}")
    private String host;

    @Value("${rpc.server.port:9000}")
    private int port;

    private final Set<String> registeredServices = ConcurrentHashMap.newKeySet();

    public RpcBeanPostProcessor(ServiceProvider serviceProvider, RpcClient rpcClient) {
        this.serviceProvider = serviceProvider;
        this.rpcClient = rpcClient;
        this.serviceRegistry = new ZkServiceRegistry();
    }

    /**
     * 在对象初始化前，把配置好的 ZK 地址传给工具类
     */
    @PostConstruct
    public void init() {
        CuratorUtils.setZkAddress(zkAddress);
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

            if (!registeredServices.contains(serviceName)) {
                // 使用从配置文件读取的 host 和 port
                serviceProvider.register(bean, (Class) interfaceClass);
                serviceRegistry.register(serviceName, new InetSocketAddress(host, port));

                registeredServices.add(serviceName);
                System.out.println("【V11.0 自动化】已成功暴露服务: " + interfaceClass.getSimpleName() + " 坐标 -> " + host + ":" + port);
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