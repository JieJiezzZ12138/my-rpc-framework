package com.jiejie.rpc.core.client;

import com.jiejie.rpc.core.entity.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 客户端动态代理类：负责拦截本地方法调用，并自动转化为网络请求
 */
public class RpcClientProxy implements InvocationHandler {

    private String host;
    private int port;

    // 构造函数：告诉代理对象，一会网络请求要发给谁
    public RpcClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 1：获取代理对象
     * 这个方法会根据传入的接口类型，在内存中动态捏造一个实现了该接口的“假对象”
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(), // 类加载器
                new Class<?>[]{clazz},  // 要代理的接口
                this                    // 谁来处理具体的逻辑（就是下面那个 invoke 方法）
        );
    }

    /**
     * 2：拦截器
     * 当你调用代理对象的任何方法时，都会“掉进”这个 invoke 方法里
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1. 自动封装请求对象（不用再像 V1.0 那样手动填信封了！）
        RpcRequest request = new RpcRequest();
        // method.getDeclaringClass().getName() 就能自动拿到接口全限定名
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParamTypes(method.getParameterTypes());

        // 2. 发起网络请求
        RpcClient rpcClient = new RpcClient();
        return rpcClient.sendRequest(request, host, port);
    }
}