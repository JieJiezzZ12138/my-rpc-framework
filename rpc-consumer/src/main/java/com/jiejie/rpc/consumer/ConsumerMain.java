package com.jiejie.rpc.consumer;

import com.jiejie.rpc.api.HelloObject;
import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.core.client.NettyRpcClient;
import com.jiejie.rpc.core.client.RpcClient;
import com.jiejie.rpc.core.client.RpcClientProxy;

/**
 * RPC 消费者后端启动入口 (V6.5 负载均衡集群版)
 * <p>
 * 本类展示了在多节点（Provider 集群）环境下，客户端如何透明地发起调用。
 * 通过循环发送多次请求，可以直观地在控制台观察到底层的 LoadBalance 组件
 * 是如何将请求均匀分发到不同端口的服务端上的。
 * </p>
 *
 * @author JieJie
 * @date 2026-04-08
 */
public class ConsumerMain {
    public static void main(String[] args) {
        // 1. 初始化 Netty 传输客户端
        // 底层已自动集成 ZkServiceDiscovery 及轮询/随机负载均衡策略
        RpcClient nettyClient = new NettyRpcClient();

        // 2. 初始化动态代理工厂
        RpcClientProxy proxy = new RpcClientProxy(nettyClient);

        // 3. 获取业务接口的代理对象
        HelloService helloService = proxy.getProxy(HelloService.class);

        System.out.println("【客户端】V6.5 集群环境就绪，准备发起高频并发调用测试...");
        System.out.println("========================================================");

        // 4. 【V6.5 核心测试逻辑】：连续发起 10 次 RPC 调用
        // 验证目标：观察请求是否能按照预期（如 RoundRobin 的绝对轮流，或 Random 的概率分布）打到不同的 Provider 节点
        for (int i = 1; i <= 10; i++) {
            try {
                // 构造带有序号的测试消息，方便在服务端日志中追踪
                String message = "Hello ZK Cluster! 消息序号: [" + i + "]";
                HelloObject requestObj = new HelloObject(i, message);

                // 发起调用（此操作会触发底层的路由算法）
                String result = helloService.sayHello(requestObj);

                System.out.println("【客户端】第 " + i + " 次调用成功，收到响应结果: " + result);
                System.out.println("--------------------------------------------------------");

                // 稍微休眠 1 秒，避免日志刷屏过快，方便肉眼观察负载均衡的调度过程
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                System.err.println("【客户端测试异常】调用过程被中断：" + e.getMessage());
            }
        }

        System.out.println("【客户端】V6.5 负载均衡压测结束。");
    }
}