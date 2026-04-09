package com.jiejie.rpc.provider;

import com.jiejie.rpc.core.server.RpcServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.Resource;

/**
 * RPC 服务提供方启动入口 (V14.5 公网部署版)
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.jiejie.rpc")
public class ProviderMain1 implements CommandLineRunner {

    // 这里注入的是我们在下文配置类中定义的公网版 RpcServer
    @Resource
    private RpcServer rpcServer;

    public static void main(String[] args) {
        SpringApplication.run(ProviderMain1.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=================================================");
        System.out.println("【公网部署】正在阿里云环境开启 Netty 引擎...");
        System.out.println("【监听地址】39.107.74.108:9000");
        System.out.println("=================================================");

        // 开启网络监听
        rpcServer.start();
    }
}