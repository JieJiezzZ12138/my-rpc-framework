package com.jiejie.rpc.core.client;

import com.jiejie.rpc.core.entity.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 客户端服务代理工厂。
 * 基于 JDK 动态代理机制实现 {@link InvocationHandler} 接口，
 * 负责拦截接口方法调用并将其透明地封装为远程过程调用请求 (RPC Request)。
 *
 * @author jiejie
 */
public class RpcClientProxy implements InvocationHandler {

    private final String host;
    private final int port;

    /**
     * 构造代理工厂，指定远程服务节点的网络位置。
     *
     * @param host 远程服务端 IP 地址
     * @param port 远程服务端监听端口
     */
    public RpcClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 生成指定接口的代理实例。
     * 使用 {@code java.lang.reflect.Proxy.newProxyInstance} 方法在运行时动态构建接口实现类。
     *
     * @param clazz 目标接口的 Class 对象
     * @param <T>   接口类型
     * @return      实现了目标接口的代理实例
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                this
        );
    }

    /**
     * 核心拦截逻辑。
     * 当代理实例的方法被调用时，自动提取调用元数据并启动网络传输。
     *
     * @param proxy  被调用的代理实例
     * @param method 被调用的方法元数据
     * @param args   调用时传入的实际参数数组
     * @return       远程服务端返回的处理结果
     * @throws Throwable 抛出调用过程中的各类异常
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1. 提取方法元数据并封装为通用的 RPC 请求协议报文
        RpcRequest request = new RpcRequest();
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParamTypes(method.getParameterTypes());

        // 2. 委派网络传输组件发起同步请求
        RpcClient rpcClient = new RpcClient();
        return rpcClient.sendRequest(request, host, port);
    }
}