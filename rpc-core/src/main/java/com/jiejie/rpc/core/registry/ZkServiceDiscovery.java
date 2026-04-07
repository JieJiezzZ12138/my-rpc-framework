package com.jiejie.rpc.core.registry;

import com.jiejie.rpc.core.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;
import java.net.InetSocketAddress;
import java.util.List;

public class ZkServiceDiscovery implements ServiceDiscovery {
    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            CuratorFramework zkClient = CuratorUtils.getZkClient();
            String servicePath = "/my-rpc/" + serviceName;
            List<String> children = zkClient.getChildren().forPath(servicePath);
            if (children == null || children.isEmpty()) throw new RuntimeException("未找到服务");

            // 简单取第一个（后续 V6.5 加入负载均衡）
            String address = children.get(0);
            String[] parts = address.split(":");
            return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}