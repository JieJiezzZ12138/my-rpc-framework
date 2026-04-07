package com.jiejie.rpc.consumer;

import com.jiejie.rpc.api.HelloObject;
import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.core.client.NettyRpcClient;
import com.jiejie.rpc.core.client.RpcClient;
import com.jiejie.rpc.core.client.RpcClientProxy;

/**
 * V6.0 客户端启动入口：体验“全自动导航”的 RPC 调用
 */
public class ConsumerMain {
    public static void main(String[] args) {
        // 1. 实例化 Netty 客户端（注意：构造函数不再需要传 IP 端口）
        RpcClient nettyClient = new NettyRpcClient();

        // 2. 创建代理对象
        RpcClientProxy proxy = new RpcClientProxy(nettyClient);

        // 3. 获取服务代理
        HelloService helloService = proxy.getProxy(HelloService.class);

        // 4. 发起调用：此刻底层会触发 ZK Lookup -> Netty Connect -> 得到结果
        System.out.println("【客户端】准备发起分布式调用...");
        String result = helloService.sayHello(new HelloObject(6, "Hello ZK!"));

        System.out.println("【客户端】收到响应结果: " + result);
    }
}