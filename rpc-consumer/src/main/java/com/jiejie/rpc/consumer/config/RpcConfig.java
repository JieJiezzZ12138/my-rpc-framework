package com.jiejie.rpc.consumer.config;

import com.jiejie.rpc.core.client.NettyRpcClient;
import com.jiejie.rpc.core.client.RpcClient;
import com.jiejie.rpc.core.provider.DefaultServiceProvider;
import com.jiejie.rpc.core.provider.ServiceProvider;
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
}