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

    @Bean
    public RpcServer rpcServer(ServiceProvider serviceProvider) {
        return new NettyRpcServer("127.0.0.1", 9000, serviceProvider);
    }
}