package com.jiejie.rpc.core.client;

import com.jiejie.rpc.core.entity.RpcRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 极简版 RPC 客户端：负责向服务端发送请求并接收结果
 */
public class RpcClient {

    /**
     * 发送网络请求
     * @param request 封装好的请求对象（信封）
     * @param host    服务端的 IP 地址
     * @param port    服务端的端口
     * @return        服务端返回的结果
     */
    public Object sendRequest(RpcRequest request, String host, int port) {
        // Socket 用来和远端服务器建立连接
        try (Socket socket = new Socket(host, port)) {
            // 1. 获取输出流：把我们的“信封”写给服务端
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(request);
            objectOutputStream.flush(); // 刷新流，确保数据发送出去

            // 2. 获取输入流：读取服务端的处理结果
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();

        } catch (Exception e) {
            System.err.println("【RPC 客户端】调用远程服务失败...");
            e.printStackTrace();
            return null;
        }
    }
}