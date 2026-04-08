package com.jiejie.rpc.consumer;

import com.jiejie.rpc.api.HelloObject;
import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.core.annotation.RpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // 魔法注入：Spring 启动时会自动把远程调用代理塞进来
    @RpcReference
    private HelloService helloService;

    /**
     * 测试地址：http://localhost:8080/test?msg=JieJie
     */
    @GetMapping("/test")
    public String test(@RequestParam("msg") String msg) {
        // 构造你在 ConsumerMain 里写的那个业务对象
        HelloObject object = new HelloObject(1, msg);

        // 发起调用（此时会触发 Netty 发包到 9000 端口的服务端）
        System.out.println("【客户端】发起自动代理调用...");
        return helloService.sayHello(object);
    }
}