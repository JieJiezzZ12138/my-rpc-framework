package com.jiejie.rpc.core.loadbalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡策略实现类 (Round Robin)。
 * <p>
 * 核心逻辑：维护一个全局自增计数器，依次将请求分配给集群中的每一个节点，保证请求被绝对均匀地分发。
 * 适用场景：集群中各个服务提供者的硬件配置和处理能力基本一致。
 * </p>
 *
 * @author JieJie
 * @date 2026-04-08
 */
public class RoundRobinLoadBalance implements LoadBalance {

    /**
     * 原子轮询计数器。
     * 使用 AtomicInteger 确保在多线程高并发调用场景下，计数器的递增操作具备原子性（线程安全），避免路由错乱。
     */
    private final AtomicInteger roundRobinId = new AtomicInteger(0);

    /**
     * 执行绝对轮询路由算法。
     *
     * @param addressList 当前在线的服务端地址列表
     * @return 按轮询顺序计算得出的目标服务地址
     */
    @Override
    public String selectServiceAddress(List<String> addressList) {
        // 1. 若集群中仅存在单一节点，则直接返回，略过取模计算
        if (addressList.size() == 1) {
            return addressList.get(0);
        }

        // 2. 获取当前请求的全局递增序号，并使其安全自增 (相当于线程安全的 i++)
        int currentId = roundRobinId.getAndIncrement();

        // 3. 核心算法：通过序号对可用节点数量进行取模运算 (Modulo)
        // 注意点：使用 Math.abs 是为了防止 currentId 在长期运行后溢出变为负数，从而导致数组越界异常
        int index = Math.abs(currentId % addressList.size());

        System.out.println("【负载均衡】触发[轮询]策略，命中节点索引：" + index + "，目标地址：" + addressList.get(index));

        return addressList.get(index);
    }
}