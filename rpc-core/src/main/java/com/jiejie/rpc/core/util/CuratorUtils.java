package com.jiejie.rpc.core.util;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Zookeeper 客户端工具类 (基于 Apache Curator)
 * 修复版：强制对准阿里云中介所
 */
public class CuratorUtils {

    // 【核心修复】把写死的本地地址，换成阿里云的公网 ZK 地址！
    private static final String ZK_ADDRESS = "39.107.74.108:2181";

    private static CuratorFramework zkClient;

    public static CuratorFramework getZkClient() {
        if (zkClient != null && zkClient.getState() == org.apache.curator.framework.imps.CuratorFrameworkState.STARTED) {
            return zkClient;
        }

        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);

        zkClient = CuratorFrameworkFactory.builder()
                .connectString(ZK_ADDRESS)    // 现在它终于知道去连阿里云了
                .retryPolicy(retryPolicy)
                .build();

        zkClient.start();

        System.out.println("【ZK 工具类】已成功建立与 Zookeeper 的连接会话 (" + ZK_ADDRESS + ")");
        return zkClient;
    }
}