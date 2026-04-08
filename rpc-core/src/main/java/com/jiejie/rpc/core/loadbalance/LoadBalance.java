package com.jiejie.rpc.core.loadbalance;

import java.util.List;

/**
 * 负载均衡策略通用接口。
 * <p>
 * 核心职责：当客户端通过注册中心发现多个可用的服务提供者（Provider）时，
 * 此组件负责根据特定的路由算法（如随机、轮询、一致性哈希等），从中筛选出唯一的最终调用目标。
 * </p>
 *
 * @author JieJie
 * @date 2026-04-08
 */
public interface LoadBalance {

    /**
     * 在可用的服务节点列表中，应用路由算法选择一个目标节点。
     *
     * @param addressList 当前在线的服务端地址列表（格式通常为 "IP:Port" 的字符串集合）
     * @return 经过算法计算后，最终选定的目标服务地址
     */
    String selectServiceAddress(List<String> addressList);
}