package com.jiejie.rpc.core.client;

import com.jiejie.rpc.core.entity.RpcRequest;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 客户端 RPC 动态代理处理器 (V5.0 重构版)。
 * 剥离了底层网络通信（Socket/Netty）细节，通过 RpcClient 接口实现网络层的彻底解耦。
 * * @author jiejie
 */
public class RpcClientProxy implements InvocationHandler {

    /**
     * 核心变动：不再硬编码 host 和 port，而是依赖抽象的 RpcClient 接口。
     * 这样就可以无缝切换 NettyRpcClient 或其他自定义客户端。
     */
    private final RpcClient client;

    public RpcClientProxy(RpcClient client) {
        this.client = client;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构建请求报文，明确指定接口全限定名以支撑服务端的动态路由
        RpcRequest request = new RpcRequest(
                method.getDeclaringClass().getCanonicalName(),
                method.getName(),
                args,
                method.getParameterTypes()
        );

        // 核心变动：将繁琐的网络 I/O 操作委派给具体的 RpcClient 实现类
        return client.sendRequest(request);
    }
}