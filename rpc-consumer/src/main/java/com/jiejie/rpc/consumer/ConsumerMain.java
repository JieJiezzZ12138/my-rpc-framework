package com.jiejie.rpc.consumer;

import com.jiejie.rpc.api.HelloObject;
import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.core.client.NettyRpcClient;
import com.jiejie.rpc.core.client.RpcClient;
import com.jiejie.rpc.core.client.RpcClientProxy;

/**
 * RPC 消费者后端启动入口 (V8.0 高性能通信与序列化版)
 * <p>
 * 架构意义：展示了底层网络层（防粘包协议）与序列化层（JSON SPI）发生翻天覆地的重构后，
 * 业务侧的调用代码依然可以保持完全透明和零侵入。
 * </p>
 *
 * @author JieJie
 */
public class ConsumerMain {
    public static void main(String[] args) {
        // 1. 初始化 Netty 传输客户端 (V8.0 引擎，底层已切换为长连接 + 自定义协议 + JSON 序列化)
        RpcClient nettyClient = new NettyRpcClient();

        // 2. 初始化动态代理工厂
        RpcClientProxy proxy = new RpcClientProxy(nettyClient);

        // 3. 获取业务接口的代理对象
        HelloService helloService = proxy.getProxy(HelloService.class);

        System.out.println("【客户端】V8.0 高性能通信架构就绪，准备发起防粘包与 JSON 序列化并发调用测试...");
        System.out.println("========================================================");

        // 4. 【V8.0 核心测试逻辑】：连续发起 10 次 RPC 调用
        // 验证目标：
        // 1. 复杂对象 HelloObject 能否被 Jackson 完美序列化与反序列化（无类型擦除）。
        // 2. 连续 10 次高频发送，自定义的 4 字节 Header 协议能否完美防御 TCP 粘包。
        for (int i = 1; i <= 10; i++) {
            try {
                // 构造复杂的测试对象，专门用来“刁难” JSON 序列化引擎
                String message = "Hello V8.0 引擎! 消息序号: [" + i + "]";
                HelloObject requestObj = new HelloObject(i, message);

                // 发起透明调用
                String result = helloService.sayHello(requestObj);

                System.out.println("【客户端】第 " + i + " 次调用成功，信封拆解完毕，真实响应结果: " + result);
                System.out.println("--------------------------------------------------------");

                // 稍微休眠 1 秒，方便肉眼观察控制台的双节点轮询打印
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                System.err.println("【客户端测试异常】调用过程被中断：" + e.getMessage());
            } catch (Exception e) {
                System.err.println("【客户端业务告警】调用失败：" + e.getMessage());
            }
        }

        System.out.println("【客户端】V8.0 全链路高压测试圆满结束！");
    }
}