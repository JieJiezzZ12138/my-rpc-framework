package com.jiejie.rpc.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * RPC 客户端 (Windows 发射阵地 - 纯净启动版)
 * <p>
 * 职责：只负责启动 Spring 容器和 Tomcat。
 * 真正的跨网 RPC 调用交由 TestController 触发。
 * </p>
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.jiejie.rpc") // 确保扫描到你的 RpcClient 和 TestController 等核心 Bean
public class ConsumerMain2 {

    public static void main(String[] args) {
        System.out.println("\n=================================================");
        System.out.println("🚀 【Windows 阵地】正在启动引擎...");
        System.out.println("=================================================");

        // 启动 Spring Boot 容器 (会自动内嵌启动 Tomcat)
        SpringApplication.run(ConsumerMain2.class, args);

        System.out.println("\n=================================================");
        System.out.println("✅ 【启动成功】请打开浏览器访问：");
        System.out.println("👉 http://localhost:8081/test?msg=JieJie");
        System.out.println("=================================================\n");
    }
}