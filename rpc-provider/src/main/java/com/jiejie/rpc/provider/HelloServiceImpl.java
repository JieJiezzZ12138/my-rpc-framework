package com.jiejie.rpc.provider;

import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.api.HelloObject;
import com.jiejie.rpc.core.annotation.RpcService;
import org.springframework.stereotype.Service;

/**
 * HelloService 接口的具体业务实现类 (V11.0 Spring 自动化版)
 * <p>
 * 变更说明：
 * 1. 增加 @RpcService：由 RpcBeanPostProcessor 识别，自动完成本地 ServiceProvider 挂载和远程 ZK 注册。
 * 2. 增加 @Service：将该实现类注入 Spring 容器。
 * </p>
 *
 * @author JieJie
 * @date 2026-04-08
 */
@RpcService // 👈 框架灵魂注解：标记该类为 RPC 服务提供者
@Service    // 👈 Spring 灵魂注解：将其声明为一个 Bean
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(HelloObject object) {
        // V9.0 心跳包会在 Handler 层被拦截，这里只处理真实的业务逻辑
        System.out.println("【服务端】正在处理业务请求，内容：" + object.getMessage());

        try {
            // 模拟业务耗时（建议保持 3s，用于验证 Netty 异步模型的稳定性）
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            System.err.println("【服务端异常】业务线程中断：" + e.getMessage());
            e.printStackTrace();
        }

        return "【V11.0 响应】RPC 自动化调用成功，收到：" + object.getMessage();
    }
}