package com.jiejie.rpc.provider;

import com.jiejie.rpc.api.HelloObject;
import com.jiejie.rpc.api.HelloService;

/**
 * 服务端对接口的具体实现
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(HelloObject object) {
        // 打印接收到的客户端消息，证明服务端确实被调用了
        System.out.println("【服务端】接收到消息: " + object.getMessage());

        // 返回处理后的结果给客户端
        return "服务端已处理你的请求，对象 ID 为: " + object.getId();
    }
}