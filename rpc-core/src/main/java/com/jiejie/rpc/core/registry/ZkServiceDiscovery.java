package com.jiejie.rpc.core.registry;

import com.jiejie.rpc.core.loadbalance.LoadBalance;
import com.jiejie.rpc.core.loadbalance.RandomLoadBalance;
import com.jiejie.rpc.core.loadbalance.RoundRobinLoadBalance;
import com.jiejie.rpc.core.util.CuratorUtils;
import com.jiejie.rpc.core.util.PropertiesUtil;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 基于 Zookeeper 的服务发现实现类 (V6.5 动态配置版)
 *
 * @author JieJie
 * @date 2026-04-08
 */
public class ZkServiceDiscovery implements ServiceDiscovery {

    private final LoadBalance loadBalance;

    public ZkServiceDiscovery() {
        // 1. 从配置文件中读取负载均衡策略，默认使用 "random"
        String strategy = PropertiesUtil.getString("rpc.loadbalance.strategy", "random");

        // 2. 根据配置动态实例化对应的策略类 (简单工厂模式雏形)
        if ("roundrobin".equalsIgnoreCase(strategy)) {
            this.loadBalance = new RoundRobinLoadBalance();
            System.out.println("【服务发现】已初始化 [轮询 - RoundRobin] 负载均衡器");
        } else {
            this.loadBalance = new RandomLoadBalance();
            System.out.println("【服务发现】已初始化 [随机 - Random] 负载均衡器");
        }
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        // ... (下方的 lookupService 代码完全保持不变，不要改动) ...
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