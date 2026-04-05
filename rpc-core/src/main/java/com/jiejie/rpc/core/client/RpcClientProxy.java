package com.jiejie.rpc.core.client;

import com.jiejie.rpc.core.entity.RpcRequest;
import java.io.*;
import java.lang.reflect.*;
import java.net.Socket;

/**
 * 客户端 RPC 动态代理处理器。
 * 自动填充接口元数据至 RpcRequest，实现跨网络调用的透明化。
 * * @author jiejie
 */
public class RpcClientProxy implements InvocationHandler {

    private final String host;
    private final int port;

    public RpcClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
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

        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(request);
            out.flush();
            return in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}