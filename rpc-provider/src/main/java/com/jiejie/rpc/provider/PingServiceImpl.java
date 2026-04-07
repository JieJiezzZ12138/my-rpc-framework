package com.jiejie.rpc.provider;

import com.jiejie.rpc.api.PingService;

/**
 * PingService 接口的本地业务实现类
 * 主要用于客户端对服务端进行健康检查、心跳探测以及基础的网络连通性测试
 *
 * @author JieJie
 * @date 2026-04-07
 */
public class PingServiceImpl implements PingService {

    /**
     * 响应心跳探测请求
     * * @return 包含版本信息的固定回执字符串 "Pong!"
     */
    @Override
    public String ping() {
        // 返回包含 V5.0 标识的响应，用于区分不同版本的容器环境
        return "Pong! 来自 V5.0 Netty 容器的响应";
    }
}