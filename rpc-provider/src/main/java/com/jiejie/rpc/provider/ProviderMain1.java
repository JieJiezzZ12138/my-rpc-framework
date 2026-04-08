package com.jiejie.rpc.provider;

import com.jiejie.rpc.core.server.RpcServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.Resource;

/**
 * RPC 服务提供方启动入口 (V11.0 Spring 自动化 + 心跳保活版)
 * <p>
 * 职责：
 * 1. 依靠 @SpringBootApplication 启动 Spring 容器。
 * 2. 依靠 @ComponentScan 触发 RpcBeanPostProcessor 的自动化扫描。
 * 3. 依靠 CommandLineRunner 在容器就绪后自动“点火” Netty。
 * </p>
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.jiejie.rpc") // 核心：必须扫到 core 里的 BeanPostProcessor
public class ProviderMain1 implements CommandLineRunner {

    // 从 Spring 容器中自动获取已经配置好的 RpcServer
    @Resource
    private RpcServer rpcServer;

    public static void main(String[] args) {
        // 启动 Spring Boot
        SpringApplication.run(ProviderMain1.class, args);
    }

    @Override
    public void run(String... args) {
        // 当所有 @RpcService 标记的类都被自动注册到 ZK 后，开启网络监听
        System.out.println("【V11.0 启动】Spring 容器初始化完毕，正在开启 Netty 引擎...");
        rpcServer.start();
    }
}