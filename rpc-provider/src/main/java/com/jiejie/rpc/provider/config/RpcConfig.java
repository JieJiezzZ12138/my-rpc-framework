package com.jiejie.rpc.provider.config;

import com.jiejie.rpc.core.client.NettyRpcClient;
import com.jiejie.rpc.core.client.RpcClient;
import com.jiejie.rpc.core.provider.DefaultServiceProvider;
import com.jiejie.rpc.core.provider.ServiceProvider;
import com.jiejie.rpc.core.server.NettyRpcServer;
import com.jiejie.rpc.core.server.RpcServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RpcConfig {

    @Bean
    public ServiceProvider serviceProvider() {
        return new DefaultServiceProvider();
    }

    @Bean
    public RpcClient rpcClient() {
        return new NettyRpcClient();
    }

    /**
     * 定义 RpcServer Bean
     * 核心改动：将 127.0.0.1 替换为阿里云公网 IP，实现跨网服务注册与暴露
     */
    @Bean
    public RpcServer rpcServer(ServiceProvider serviceProvider) {
        // 参数说明：阿里云公网IP, 监听端口, 服务注册器
        return new NettyRpcServer("39.107.74.108", 9000, serviceProvider);
    }
}