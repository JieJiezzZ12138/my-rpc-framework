package com.jiejie.rpc.consumer;

import com.jiejie.rpc.api.HelloObject;
import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.core.client.RpcClientProxy;

/**
 * 客户端启动入口（V2.0 代理版）
 */
public class ConsumerMain {
    public static void main(String[] args) {
        // 1. 创建一个代理工厂对象，告诉它服务端的地址
        RpcClientProxy proxy = new RpcClientProxy("127.0.0.1", 9000);

        // 2. 通过代理工厂，直接获取 HelloService 接口的实例
        // 注意：这里拿到的其实是一个“假”的实现类，它内部会帮我们发网络请求
        HelloService helloService = proxy.getProxy(HelloService.class);

        // 3. 准备测试数据
        HelloObject object = new HelloObject(666, "你好！我是 V2.0 代理版的调用！");

        // 4. 像调用本地方法一样自然！
        // 你点进这个 sayHello 方法，IDEA 甚至会带你跳转到 api 模块的接口定义上
        String result = helloService.sayHello(object);

        // 5. 打印结果
        System.out.println("【客户端】收到响应结果: " + result);
    }
}