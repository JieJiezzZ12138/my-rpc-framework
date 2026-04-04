package com.jiejie.rpc.core.server;

import com.jiejie.rpc.core.entity.RpcRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * RPC 服务端核心组件。
 * 采用同步阻塞 I/O (BIO) 网络模型，监听特定端口并根据接收到的请求元数据，
 * 利用 Java 反射机制动态调用本地服务实现。
 *
 * @author jiejie
 */
public class RpcServer {

    /**
     * 启动 RPC 服务监听。
     * 该方法会阻塞当前线程，持续接收并处理来自客户端的远程调用请求。
     *
     * @param service 暴露给远程调用的服务实现类实例
     * @param port    服务端监听的端口号
     */
    public void start(Object service, int port) {
        // 初始化 ServerSocket 并绑定监听端口，利用 try-with-resources 确保资源自动释放
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("RPC Server is running on port: " + port);

            Socket socket;
            // 持续循环以监听连接请求。当前采用单线程同步处理模型，在高并发场景下存在性能瓶颈
            while ((socket = serverSocket.accept()) != null) {
                System.out.println("Connection established. Remote IP: " + socket.getInetAddress());

                // 1. 反序列化阶段：从网络输入流中读取并重建 RpcRequest 协议对象
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                RpcRequest request = (RpcRequest) objectInputStream.readObject();

                // 2. 反射调用阶段：根据请求中的方法名与参数类型，在本地服务实例中定位并执行目标方法
                // method.getParamTypes() 确保了在存在方法重载时的唯一性
                Method method = service.getClass().getMethod(request.getMethodName(), request.getParamTypes());
                Object result = method.invoke(service, request.getParameters());

                // 3. 响应回写阶段：将方法执行结果序列化后通过输出流返回给调用方
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(result);
                objectOutputStream.flush();

                // 单次请求处理完毕，释放当前 Socket 连接资源
                objectInputStream.close();
                objectOutputStream.close();
                socket.close();
            }
        } catch (Exception e) {
            // 异常捕获建议在后续版本中引入专业的日志框架进行记录
            System.err.println("An error occurred in RpcServer during execution.");
            e.printStackTrace();
        }
    }
}