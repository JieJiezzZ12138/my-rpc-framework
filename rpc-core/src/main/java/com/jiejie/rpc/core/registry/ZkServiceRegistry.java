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
            // 创建【临时节点】，服务端断开连接后自动删除
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
            System.out.println("【ZK 注册】服务 " + serviceName + " 已挂载至 " + inetSocketAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}