package com.jiejie.rpc.core.util;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Zookeeper 客户端工具类
 */
public class CuratorUtils {
    private static final String ZK_ADDRESS = "127.0.0.1:2181";
    private static CuratorFramework zkClient;

    public static CuratorFramework getZkClient() {
        if (zkClient != null && zkClient.getState() == org.apache.curator.framework.imps.CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        // 重试策略：每隔 1s 重试一次，最多 3 次
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(ZK_ADDRESS)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        return zkClient;
    }
}