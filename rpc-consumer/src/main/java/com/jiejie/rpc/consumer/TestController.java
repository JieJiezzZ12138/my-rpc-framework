package com.jiejie.rpc.consumer;

import com.jiejie.rpc.api.HelloObject;
import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.core.annotation.RpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // 魔法注入：Spring 启动时会自动把远程调用代理塞进来，日志已证明注入成功！
    @RpcReference
    private HelloService helloService;

    /**
     * 测试地址：http://localhost:8081/test?msg=JieJie
     * (注意：根据你的启动日志，Tomcat 运行在 8081 端口)
     */
    @GetMapping("/test")
    public String test(@RequestParam(value = "msg", defaultValue = "这是来自 Windows 浏览器的默认跨网呼叫！") String msg) {

        // 1. 构造你在 ConsumerMain 里写的那个业务对象
        HelloObject object = new HelloObject(1, msg);

        // 2. 发起调用（此时会触发 Netty 发包到阿里云的 9000 端口）
        System.out.println("🚀 【客户端】收到浏览器请求，正在向阿里云发起 RPC 调用...");

        // 3. 返回给浏览器
        return helloService.sayHello(object);
    }
}