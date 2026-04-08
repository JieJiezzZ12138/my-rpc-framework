package com.jiejie.rpc.core.loadbalance;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡策略实现类 (Random)。
 * <p>
 * 核心逻辑：基于伪随机数生成器，在可用节点列表中随机抽取一个索引进行路由。
 * 适用场景：调用量足够大时，请求会统计学意义上均匀分布在各个节点。
 * </p>
 *
 * @author JieJie
 * @date 2026-04-08
 */
public class RandomLoadBalance implements LoadBalance {

    /** * 随机数生成器。
     * 提示：如果是极高并发场景，未来可考虑升级为 ThreadLocalRandom 以减少线程竞争。
     */
    private final Random random = new Random();

    /**
     * 执行随机路由算法。
     *
     * @param addressList 当前在线的服务端地址列表
     * @return 随机抽取的目标服务地址
     */
    @Override
    public String selectServiceAddress(List<String> addressList) {
        // 1. 若集群中仅存在单一节点，则直接返回，略过计算
        if (addressList.size() == 1) {
            return addressList.get(0);
        }

        // 2. 生成范围在 [0, 列表长度) 的随机整数作为目标索引
        int index = random.nextInt(addressList.size());

        System.out.println("【负载均衡】触发[随机]策略，命中节点索引：" + index + "，目标地址：" + addressList.get(index));

        return addressList.get(index);
    }
}