package com.jiejie.rpc.core.server;

import com.jiejie.rpc.core.entity.RpcRequest;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * RPC 请求处理任务。
 * 负责解析 Socket 流中的 RpcRequest，利用反射调用目标方法并回写结果。
 * * @author jiejie
 */
public class RequestHandlerThread implements Runnable {

    private final Socket socket;
    private final Object service;

    public RequestHandlerThread(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    @Override
    public void run() {
        // 利用 try-with-resources 确保 Socket 资源在任务结束时自动关闭
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {

            // 1. 反序列化请求对象
            RpcRequest request = (RpcRequest) objectInputStream.readObject();

            // 2. 通过反射定位并执行目标方法
            Method method = service.getClass().getMethod(request.getMethodName(), request.getParamTypes());
            Object result = method.invoke(service, request.getParameters());

            // 3. 将结果写回输出流
            objectOutputStream.writeObject(result);
            objectOutputStream.flush();

        } catch (Exception e) {
            System.err.println("Error occurred during request handling: " + e.getMessage());
            e.printStackTrace();
        }
    }
}