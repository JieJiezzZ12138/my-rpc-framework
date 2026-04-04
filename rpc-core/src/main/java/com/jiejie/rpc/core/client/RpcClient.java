package com.jiejie.rpc.core.client;

import com.jiejie.rpc.core.entity.RpcRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 远程调用客户端网络传输组件。
 * 采用同步阻塞 I/O (BIO) 模型，负责建立 Socket 连接并完成请求报文的序列化传输与响应接收。
 * * @author jiejie
 */
public class RpcClient {

    /**
     * 发送 RPC 请求并同步获取执行结果。
     * * @param request 封装了调用元数据的 {@link RpcRequest} 实体
     * @param host    远程服务端的主机 IP 地址
     * @param port    远程服务端监听的端口号
     * @return        服务端执行后的返回对象；若调用异常则返回 null
     */
    public Object sendRequest(RpcRequest request, String host, int port) {
        // 使用 try-with-resources 自动管理 Socket 及其流资源的释放
        try (Socket socket = new Socket(host, port)) {
            // 1. 获取输出流：将请求对象序列化并写入网络缓冲区
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();

            // 2. 获取输入流：阻塞等待服务端处理完毕并返回响应报文
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();

        } catch (Exception e) {
            // 在生产环境下建议引入日志框架（如 SLF4J）进行异常堆栈记录
            System.err.println("Remote invocation failed during network transport.");
            e.printStackTrace();
            return null;
        }
    }
}