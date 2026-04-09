package com.jiejie.rpc.core.util;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Zookeeper 客户端工具类 (基于 Apache Curator)
 * 进化版：支持动态配置地址，拒绝硬编码
 */
public class CuratorUtils {

    // 默认地址（内网回环），会被 Spring 扫描到的配置覆盖
    private static String zkAddress = "127.0.0.1:2181";

    private static CuratorFramework zkClient;

    /**
     * 提供给外部（如 RpcBeanPostProcessor）在启动时设置真实的 ZK 地址
     */
    public static void setZkAddress(String address) {
        zkAddress = address;
    }

    public static CuratorFramework getZkClient() {
        if (zkClient != null && zkClient.getState() == org.apache.curator.framework.imps.CuratorFrameworkState.STARTED) {
            return zkClient;
        }

        // 指数退避重试策略
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);

        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zkAddress) // 使用变量而不是硬编码
                .retryPolicy(retryPolicy)
                .build();

        zkClient.start();

        System.out.println("【ZK 工具类】正在尝试连接 Zookeeper -> " + zkAddress);
        return zkClient;
    }
}