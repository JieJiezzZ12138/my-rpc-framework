package com.jiejie.rpc.consumer;

import com.jiejie.rpc.api.HelloObject;
import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.api.PingService;
import com.jiejie.rpc.core.client.NettyRpcClient;
import com.jiejie.rpc.core.client.RpcClient;
import com.jiejie.rpc.core.client.RpcClientProxy;

/**
 * 客户端并发测试入口（V5.0 Netty 重构版）。
 * 验证底层通信切换为 Netty NIO 后，多服务的动态路由与非阻塞并发能力。
 *
 * @author jiejie
 */
public class ConsumerMain {
    public static void main(String[] args) {
        // 1. 初始化 Netty 客户端（底层网络传输已由 BIO 切换为 NIO）
        RpcClient nettyClient = new NettyRpcClient("127.0.0.1", 9000);

        // 2. 将具体通信实现注入代理类，实现网络层的彻底解耦
        RpcClientProxy proxy = new RpcClientProxy(nettyClient);

        // 3. 获取多个不同业务接口的代理实例
        HelloService helloService = proxy.getProxy(HelloService.class);
        PingService pingService = proxy.getProxy(PingService.class);

        // 线程 A：调用耗时的业务服务 (模拟重度计算或 DB 查询)
        new Thread(() -> {
            long start = System.currentTimeMillis();
            System.out.println("【线程 A】开始请求 HelloService...");

            String res = helloService.sayHello(new HelloObject(1, "Netty NIO Test"));

            long end = System.currentTimeMillis();
            System.out.println("【线程 A】返回结果: " + res + " | 实际耗时: " + (end - start) + "ms");
        }).start();

        // 线程 B：调用即时响应的探测服务 (验证 Netty 的高吞吐与非阻塞)
        new Thread(() -> {
            // 故意延迟 100ms，确保线程 A 的长耗时请求先发出去
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}

            long start = System.currentTimeMillis();
            System.out.println("【线程 B】开始请求 PingService...");

            String res = pingService.ping();

            long end = System.currentTimeMillis();
            System.out.println("【线程 B】返回结果: " + res + " | 实际耗时: " + (end - start) + "ms");
        }).start();
    }
}