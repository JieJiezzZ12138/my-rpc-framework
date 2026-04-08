package com.jiejie.rpc.provider;

import com.jiejie.rpc.api.PingService;
import com.jiejie.rpc.core.annotation.RpcService;
import org.springframework.stereotype.Service;

/**
 * PingService 接口的本地业务实现类 (V11.0 Spring 自动化版)
 * <p>
 * 职责：用于业务层面的连通性测试。
 * 注意：这与底层 Netty 的心跳包不同，它代表了 Spring 容器和业务反射链路的完整畅通。
 * </p>
 *
 * @author JieJie
 * @date 2026-04-08
 */
@RpcService // 👈 让框架自动扫描并注册
@Service    // 👈 让 Spring 容器管理
public class PingServiceImpl implements PingService {

    @Override
    public String ping() {
        // 更新回执信息，见证框架的进化
        return "Pong! 来自 V11.0 Spring 全自动容器的响应";
    }
}