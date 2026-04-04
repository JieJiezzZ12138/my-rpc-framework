package com.jiejie.rpc.consumer;

import com.jiejie.rpc.api.HelloObject;
import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.core.client.RpcClientProxy;

/**
 * 客户端启动入口（V2.0 代理版本）。
 * 演示通过 JDK 动态代理实现远程服务的透明化调用，屏蔽底层复杂的网络通信逻辑。
 * * @author jiejie
 */
public class ConsumerMain {

    public static void main(String[] args) {
        // 1. 初始化客户端代理工厂，指定远程服务节点的网络拓扑地址（IP与端口）
        RpcClientProxy proxy = new RpcClientProxy("127.0.0.1", 9000);

        // 2. 基于接口类型获取远程服务的代理实例
        // 采用接口感知的调用模式，符合面向接口编程原则，解耦业务逻辑与基础设施
        HelloService helloService = proxy.getProxy(HelloService.class);

        // 3. 构建测试用的业务数据传输对象 (DTO)
        HelloObject object = new HelloObject(666, "RPC Remote Call Test - V2.0");

        // 4. 执行远程过程调用 (RPC)
        // 代理内部自动完成调用拦截、请求封装、序列化及同步网络通信
        String result = helloService.sayHello(object);

        // 5. 消费并输出服务端的响应结果
        System.out.println("Result from server: " + result);
    }
}