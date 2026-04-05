package com.jiejie.rpc.provider;

import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.api.HelloObject;

/**
 * 具体的业务逻辑实现。
 * 增加 Thread.sleep 以验证 V3.0 服务端的并发处理能力。
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(HelloObject object) {
        System.out.println("【服务端】接收到消息：" + object.getMessage());

        try {
            // 模拟业务耗时，验证线程池是否起作用
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "RPC 调用成功，已处理消息内容：" + object.getMessage();
    }
}