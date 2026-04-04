package com.jiejie.rpc.consumer;

import com.jiejie.rpc.api.HelloObject;
import com.jiejie.rpc.core.client.RpcClient;
import com.jiejie.rpc.core.entity.RpcRequest;

/**
 * 客户端启动入口
 */
public class ConsumerMain {
    public static void main(String[] args) {
        // 1. 准备好负责网络通信的客户端
        RpcClient client = new RpcClient();

        // 2. 【最关键的一步】手动封装请求信息（填信封）
        // 我们要告诉服务端：去调 com.jiejie.rpc.api.HelloService 接口里的 sayHello 方法
        RpcRequest request = new RpcRequest();
        request.setInterfaceName("com.jiejie.rpc.api.HelloService");
        request.setMethodName("sayHello");
        // 传入参数值（我们在 API 里定义的实体类）
        request.setParameters(new Object[]{new HelloObject(123, "你好，我是消费者，这是我的第一次调用！")});
        // 传入参数类型（防止方法重载找不到）
        request.setParamTypes(new Class[]{HelloObject.class});

        // 3. 发起网络请求（本机测试 IP 用 127.0.0.1，端口是刚才服务端监听的 9000）
        System.out.println("【客户端】正在发起请求...");
        Object response = client.sendRequest(request, "127.0.0.1", 9000);

        // 4. 打印服务端的返回结果
        System.out.println("【客户端】收到服务端的响应: " + response);
    }
}