package com.jiejie.rpc.core.registry;

import com.jiejie.rpc.core.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import java.net.InetSocketAddress;

/**
 * 服务注册中心 Zookeeper 实现
 * 负责将本地服务暴露给 ZK 供客户端查询
 *
 * @author JieJie
 */
public class ZkServiceRegistry implements ServiceRegistry {

    /**
     * 注册服务节点
     * 创建 EPHEMERAL（临时）节点，若 Provider 宕机，ZK 会自动删除该地址
     */
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            // 定义 ZNode 路径：/my-rpc/接口全限定名/IP:Port
            String path = "/my-rpc/" + serviceName + inetSocketAddress.toString();
            CuratorFramework zkClient = CuratorUtils.getZkClient();

            // 递归创建父节点，并确保当前节点为临时节点
            zkClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(path);

            System.out.println("【ZK 注册】接口 [" + serviceName + "] 已成功挂载至：" + inetSocketAddress);
        } catch (Exception e) {
            System.err.println("【ZK 注册失败】原因：" + e.getMessage());
        }
    }
}