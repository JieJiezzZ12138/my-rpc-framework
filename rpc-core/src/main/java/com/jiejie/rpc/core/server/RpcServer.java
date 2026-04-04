package com.jiejie.rpc.core.server;

import com.jiejie.rpc.core.entity.RpcRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 极简版 RPC 服务端：负责接收网络请求并反射调用本地方法
 */
public class RpcServer {

    /**
     * 启动服务
     * @param service 我们要暴露的服务实现类对象（比如 HelloServiceImpl 的实例）
     * @param port    服务监听的端口（比如 9000）
     */
    public void start(Object service, int port) {
        // ServerSocket 是 Java 提供的基础网络类，用来监听端口
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("【RPC 服务端】已启动，正在监听端口：" + port);

            Socket socket;
            // while(true) 代表服务端会一直死循环运行，等待客户端的连接
            while ((socket = serverSocket.accept()) != null) {
                System.out.println("【RPC 服务端】有新的客户端连接进来啦！IP: " + socket.getInetAddress());

                // 1. 获取输入流：读取客户端发过来的“信封”（RpcRequest 对象）
                // ObjectInputStream 可以直接把网络中的字节流反序列化成 Java 对象
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                RpcRequest request = (RpcRequest) objectInputStream.readObject();

                // 2. 核心魔法：使用 Java 反射机制调用本地方法
                // 通过方法名和参数类型，找到我们要调用的具体 Method
                Method method = service.getClass().getMethod(request.getMethodName(), request.getParamTypes());
                // 执行该方法，传入服务实例和具体参数，拿到执行结果
                Object result = method.invoke(service, request.getParameters());

                // 3. 获取输出流：把执行结果写回给客户端
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(result);
                objectOutputStream.flush(); // 刷新流，确保数据发送出去

                // 处理完一次请求后，关闭这一根网线的连接
                objectInputStream.close();
                objectOutputStream.close();
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}