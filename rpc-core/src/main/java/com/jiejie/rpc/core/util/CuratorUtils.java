package com.jiejie.rpc.core.util;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Zookeeper 客户端工具类 (基于 Apache Curator)
 * 负责管理与分布式注册中心的安全连接、会话生命周期及重试逻辑
 *
 * @author JieJie
 * @date 2026-04-07
 */
public class CuratorUtils {

    /** Zookeeper 服务端默认监听地址与端口 */
    private static final String ZK_ADDRESS = "127.0.0.1:2181";

    /** 全局唯一的 Zookeeper 客户端实例 (单例模式) */
    private static CuratorFramework zkClient;

    /**
     * 获取并启动 Zookeeper 客户端实例
     * 采用双重检查思路确保客户端在整个运行期间仅初始化一次并持续在线
     * * @return 已启动且可用的 CuratorFramework 客户端对象
     */
    public static CuratorFramework getZkClient() {
        // 1. 检查客户端是否已存在且处于已启动状态，若是则直接返回
        if (zkClient != null && zkClient.getState() == org.apache.curator.framework.imps.CuratorFrameworkState.STARTED) {
            return zkClient;
        }

        // 2. 配置指数退避重试策略 (Exponential Backoff Retry)
        // 参数说明：初始等待时间为 1000ms，最大重试次数为 3 次
        // 该策略能有效应对网络瞬时抖动，防止盲目重连导致的服务端压力
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);

        // 3. 使用 Fluent 风格的 Builder 模式构建客户端
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(ZK_ADDRESS)    // 设置连接地址
                .retryPolicy(retryPolicy)     // 绑定重试策略
                .build();

        // 4. 显式启动客户端以建立网络长连接
        zkClient.start();

        System.out.println("【ZK 工具类】已成功建立与 Zookeeper 的连接会话");
        return zkClient;
    }
}