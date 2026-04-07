package com.jiejie.rpc.provider;

import com.jiejie.rpc.api.PingService;

public class PingServiceImpl implements PingService {
    @Override
    public String ping() {
        return "Pong! 来自 V5.0 Netty 容器的响应";
    }
}