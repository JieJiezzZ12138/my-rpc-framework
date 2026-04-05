package com.jiejie.rpc.core.server;

import com.jiejie.rpc.core.entity.RpcRequest;
import com.jiejie.rpc.core.provider.ServiceProvider;
import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * RPC 请求处理工作线程。
 * 负责解析通信协议报文，并通过 ServiceProvider 动态检索目标实例进行反射调用。
 * * @author jiejie
 */
public class RequestHandlerThread implements Runnable {

    private final Socket socket;
    private final ServiceProvider serviceProvider;

    public RequestHandlerThread(Socket socket, ServiceProvider serviceProvider) {
        this.socket = socket;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void run() {
        //线程信息打印
        String threadName = Thread.currentThread().getName();
        System.out.println("【Server 日志】当前处理线程: " + threadName + " 正在为客户端服务...");
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {

            // 反序列化请求对象
            RpcRequest request = (RpcRequest) objectInputStream.readObject();

            // 基于请求中的接口名，从注册表动态获取业务实现类
            Object service = serviceProvider.getService(request.getInterfaceName());

            // 反射定位方法并执行业务逻辑
            Method method = service.getClass().getMethod(request.getMethodName(), request.getParamTypes());
            Object result = method.invoke(service, request.getParameters());

            // 写回处理结果
            objectOutputStream.writeObject(result);
            objectOutputStream.flush();

        } catch (Exception e) {
            System.err.println("【线程异常】处理 RPC 请求时发生故障: " + e.getMessage());
            e.printStackTrace();
        }
    }
}