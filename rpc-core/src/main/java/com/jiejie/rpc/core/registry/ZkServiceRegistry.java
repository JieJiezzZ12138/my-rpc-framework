package com.jiejie.rpc.core.registry;

import com.jiejie.rpc.core.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import java.net.InetSocketAddress;

public class ZkServiceRegistry implements ServiceRegistry {
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            String path = "/my-rpc/" + serviceName + inetSocketAddress.toString();
            CuratorFramework zkClient = CuratorUtils.getZkClient();

            // 预处理：解决重启过快导致的节点残留
            if (zkClient.checkExists().forPath(path) != null) {
                zkClient.delete().forPath(path);
            }

            zkClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(path);
        } catch (Exception e) {
            System.err.println("【ZK 注册警报】" + e.getMessage());
        }
    }
}