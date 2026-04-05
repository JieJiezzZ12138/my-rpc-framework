package com.jiejie.rpc.consumer;

import com.jiejie.rpc.api.HelloObject;
import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.core.client.RpcClientProxy;

/**
 * 客户端并发测试入口（V3.0）。
 * 通过多线程模拟高并发场景，验证服务端线程池的并行处理能力。
 *
 * @author jiejie
 */
public class ConsumerMain {

    public static void main(String[] args) {
        // 1. 初始化代理工厂
        RpcClientProxy proxy = new RpcClientProxy("127.0.0.1", 9000);
        HelloService helloService = proxy.getProxy(HelloService.class);

        // 2. 模拟 2 个并发请求（可以根据需要增加循环次数）
        for (int i = 1; i <= 2; i++) {
            final int requestId = i;
            new Thread(() -> {
                long startTime = System.currentTimeMillis();

                // 构建测试 DTO
                HelloObject object = new HelloObject(requestId, "Concurrency Test - Request #" + requestId);

                try {
                    // 执行 RPC 调用
                    System.out.println("【线程 " + requestId + "】开始发起远程调用...");
                    String result = helloService.sayHello(object);

                    long endTime = System.currentTimeMillis();
                    System.out.println("【线程 " + requestId + "】收到响应: " + result);
                    System.out.println("【线程 " + requestId + "】总耗时: " + (endTime - startTime) + "ms");
                } catch (Exception e) {
                    System.err.println("【线程 " + requestId + "】调用失败: " + e.getMessage());
                }
            }).start();
        }
    }
}