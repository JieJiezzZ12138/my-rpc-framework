package com.jiejie.rpc.consumer;

import com.jiejie.rpc.api.HelloObject;
import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.core.client.NettyRpcClient;
import com.jiejie.rpc.core.client.RpcClient;
import com.jiejie.rpc.core.client.RpcClientProxy;

/**
 * RPC 消费者后端启动入口 (V6.0 分布式版本)
 * * 本类展示了如何利用服务发现机制，在不指定服务端 IP 和端口的情况下，
 * 透明地发起远程过程调用。
 *
 * @author JieJie
 * @date 2026-04-07
 */
public class ConsumerMain {
    public static void main(String[] args) {
        // 1. 初始化 Netty 传输客户端
        // 在 V6.0 中，客户端已集成 ServiceDiscovery，构造时不再需要硬编码服务端地址
        RpcClient nettyClient = new NettyRpcClient();

        // 2. 初始化动态代理工厂
        // 将具体的网络传输实现（nettyClient）注入代理类
        RpcClientProxy proxy = new RpcClientProxy(nettyClient);

        // 3. 获取业务接口的代理对象
        // 通过 JDK 动态代理生成 HelloService 的实现实例
        HelloService helloService = proxy.getProxy(HelloService.class);

        // 4. 执行远程调用
        // 核心流程：代理对象拦截调用 -> 访问 Zookeeper 获取可用服务地址 -> 建立 Netty 连接 -> 发送请求 -> 获取响应
        System.out.println("【客户端】准备发起分布式调用...");
        String result = helloService.sayHello(new HelloObject(6, "Hello ZK!"));

        System.out.println("【客户端】收到响应结果: " + result);
    }
}