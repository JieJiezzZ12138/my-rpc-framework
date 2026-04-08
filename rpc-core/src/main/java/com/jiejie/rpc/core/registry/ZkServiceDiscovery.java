package com.jiejie.rpc.core.registry;

import com.jiejie.rpc.core.loadbalance.LoadBalance;
import com.jiejie.rpc.core.loadbalance.RandomLoadBalance;
import com.jiejie.rpc.core.util.CuratorUtils;
import com.jiejie.rpc.core.util.PropertiesUtil;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 基于 Zookeeper 的服务发现实现类 (V7.0 纯动态 SPI 架构版)
 *
 * @author JieJie
 */
public class ZkServiceDiscovery implements ServiceDiscovery {

    private LoadBalance loadBalance;

    public ZkServiceDiscovery() {
        // 1. 获取用户在 properties 中配置的策略名（默认 random）
        String targetStrategy = PropertiesUtil.getString("rpc.loadbalance.strategy", "random");

        // 2. 【核心魔法】：使用 Java SPI 自动加载所有实现了 LoadBalance 接口的类
        ServiceLoader<LoadBalance> serviceLoader = ServiceLoader.load(LoadBalance.class);

        // 3. 遍历所有被 SPI 扫出来的实例，寻找名字和配置匹配的那一个
        for (LoadBalance spiInstance : serviceLoader) {
            if (spiInstance.getStrategyName().equalsIgnoreCase(targetStrategy)) {
                this.loadBalance = spiInstance;
                System.out.println("【SPI 装配】通过 SPI 机制成功加载并初始化路由策略：" + spiInstance.getClass().getSimpleName());
                break;
            }
        }

        // 4. 防御性兜底：如果用户配置了乱七八糟的名字，导致 SPI 没找到匹配的，回退到默认随机策略
        if (this.loadBalance == null) {
            System.err.println("【SPI 告警】未找到名为 [" + targetStrategy + "] 的策略实现，将降级使用默认 Random 策略。");
            this.loadBalance = new RandomLoadBalance();
        }
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        // ... (下方的核心服务查询逻辑完全不变) ...
        try {
            CuratorFramework zkClient = CuratorUtils.getZkClient();
            String servicePath = "/my-rpc/" + serviceName;
            List<String> children = zkClient.getChildren().forPath(servicePath);

            if (children == null || children.isEmpty()) {
                throw new RuntimeException("【ZK 异常】未找到任何可用的服务提供者：" + serviceName);
            }

            String address = loadBalance.selectServiceAddress(children);
            System.out.println("【ZK 发现】集群节点列表: " + children + " -> 最终选择: " + address);

            String[] parts = address.split(":");
            return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
        } catch (Exception e) {
            System.err.println("【ZK 发现失败】查询过程出现异常：" + e.getMessage());
            return null;
        }
    }
}