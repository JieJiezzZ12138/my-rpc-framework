package com.jiejie.rpc.provider;

import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.api.HelloObject;

/**
 * HelloService 接口的具体业务实现类
 * 该类作为服务端真正的逻辑执行体，被加载至 ServiceProvider 本地容器中
 *
 * @author JieJie
 * @date 2026-04-07
 */
public class HelloServiceImpl implements HelloService {

    /**
     * 处理远程招呼请求
     * * @param object 客户端传输的业务对象
     * @return 经过服务端处理后的回执字符串
     */
    @Override
    public String sayHello(HelloObject object) {
        System.out.println("【服务端】接收到 RPC 请求，消息内容：" + object.getMessage());

        try {
            // 【核心说明】：此处人为增加 3000 毫秒延迟
            // 目的：模拟复杂的业务逻辑处理耗时，用于验证 V3.0 及后续版本中服务端线程池/Netty 异步模型的并发处理能力
            // 验证场景：当多个客户端同时发起调用时，服务端是否能通过多线程并行处理，而非阻塞式排队
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            System.err.println("【服务端异常】业务处理线程被中断：" + e.getMessage());
            e.printStackTrace();
        }

        return "RPC 调用成功，已处理消息内容：" + object.getMessage();
    }
}