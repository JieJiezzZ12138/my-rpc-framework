package com.jiejie.rpc.consumer;

import com.jiejie.rpc.api.HelloObject;
import com.jiejie.rpc.api.HelloService;
import com.jiejie.rpc.core.annotation.RpcReference;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * RPC 客户端 (Windows 发射阵地)
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.jiejie.rpc") // 确保扫描到你的 RpcClient 等核心 Bean
public class ConsumerMain2 implements CommandLineRunner {

    // 核心武器：通过你的自定义注解，让框架生成动态代理
    // 当调用这个接口的方法时，实际上会被拦截并转化为网络请求
    @RpcReference
    private HelloService helloService;

    public static void main(String[] args) {
        // 启动 Spring Boot 容器
        SpringApplication.run(ConsumerMain2.class, args);
    }

    /**
     * Spring 容器启动完成后，会自动执行这个 run 方法
     */
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=================================================");
        System.out.println("🚀 【Windows 阵地】系统就绪，准备跨网呼叫阿里云...");
        System.out.println("=================================================");

        try {
            // 1. 先创建一个 HelloObject 对象 (根据你实际的实体类字段来定)
            // 通常你们教程里的 HelloObject 会包含 id 和 message 两个字段，比如：
            HelloObject object = new HelloObject(1, "JieJie 的 Windows 机器向云端发来贺电！");

            /* 如果你的 HelloObject 没有有参构造函数，可以用 set 方法：
               HelloObject object = new HelloObject();
               object.setId(1);
               object.setMessage("JieJie 的 Windows 机器向云端发来贺电！");
            */

            // 2. 把对象传给 RPC 接口
            String result = helloService.sayHello(object);

            System.out.println("\n🎉 【收到云端响应】：" + result + "\n");
        } catch (Exception e) {
            System.err.println("\n❌ 【呼叫失败】请检查阿里云防火墙、ZK 状态或 Provider 是否运行！");
            e.printStackTrace();
        }
    }
}