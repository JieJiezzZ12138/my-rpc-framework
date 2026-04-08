package com.jiejie.rpc.provider;

import com.jiejie.rpc.core.server.RpcServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.Resource;

@SpringBootApplication
@ComponentScan(basePackages = {"com.jiejie.rpc"}) // 扫描框架核心类和业务类
public class SpringProviderMain implements CommandLineRunner {

    @Resource
    private RpcServer rpcServer;

    public static void main(String[] args) {
        SpringApplication.run(SpringProviderMain.class, args);
    }

    @Override
    public void run(String... args) {
        // 当 Spring 容器启动完成后，自动开启 Netty 监听
        // 此时 RpcBeanPostProcessor 已经帮我们把所有 @RpcService 注册好了
        rpcServer.start();
    }
}