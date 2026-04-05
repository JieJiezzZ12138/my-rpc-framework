package com.jiejie.rpc.consumer;

import com.jiejie.rpc.api.HelloObject;
import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.api.PingService;
import com.jiejie.rpc.core.client.RpcClientProxy;

/**
 * 客户端并发测试入口（V3.0）。
 * 通过多线程模拟高并发场景，验证服务端线程池的并行处理能力。
 *
 * @author jiejie
 */
public class ConsumerMain {
    public static void main(String[] args) {
        RpcClientProxy proxy = new RpcClientProxy("127.0.0.1", 9000);
        HelloService helloService = proxy.getProxy(HelloService.class);
        PingService pingService = proxy.getProxy(PingService.class);

        // 线程 1：调用耗时 3 秒的服务
        new Thread(() -> {
            long start = System.currentTimeMillis();
            System.out.println("【线程 1】开始请求 HelloService...");
            String res = helloService.sayHello(new HelloObject(1, "Timer Test"));
            long end = System.currentTimeMillis();
            System.out.println("【线程 1】返回结果: " + res + " | 实际耗时: " + (end - start) + "ms");
        }).start();

        // 线程 2：调用即时响应的服务
        new Thread(() -> {
            long start = System.currentTimeMillis();
            System.out.println("【线程 2】开始请求 PingService...");
            String res = pingService.ping();
            long end = System.currentTimeMillis();
            System.out.println("【线程 2】返回结果: " + res + " | 实际耗时: " + (end - start) + "ms");
        }).start();
    }
}