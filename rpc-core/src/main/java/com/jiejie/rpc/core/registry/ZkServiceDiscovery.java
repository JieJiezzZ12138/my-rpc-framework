package com.jiejie.rpc.core.registry;

import com.jiejie.rpc.core.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * 基于 Zookeeper 的服务发现实现类
 * 客户端通过此类从 ZK 注册中心获取远程服务的真实网络地址
 *
 * @author JieJie
 * @date 2026-04-07
 */
public class ZkServiceDiscovery implements ServiceDiscovery {

    /**
     * 根据服务名称查找可用的服务端地址
     * * @param serviceName 接口全限定名 (例如: com.jiejie.rpc.api.HelloService)
     * @return 包含 IP 和端口的服务端地址对象
     */
    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            // 1. 获取已启动的 Zookeeper 客户端
            CuratorFramework zkClient = CuratorUtils.getZkClient();

            // 2. 拼接服务路径：/my-rpc/接口名
            String servicePath = "/my-rpc/" + serviceName;

            // 3. 获取该路径下的所有子节点 (即所有注册在该接口下的服务端 IP:Port 列表)
            List<String> children = zkClient.getChildren().forPath(servicePath);
            if (children == null || children.isEmpty()) {
                throw new RuntimeException("【ZK 异常】未找到任何可用的服务提供者：" + serviceName);
            }

            // 4. 【简易负载均衡】：目前直接取服务列表中的第一个地址
            // 提示：V6.5 版本将在此处引入 Random/RoundRobin 等复杂的负载均衡算法
            String address = children.get(0);
            System.out.println("【ZK 发现】已成功匹配服务地址：" + address);

            // 5. 将字符串格式的地址 (如 "127.0.0.1:9000") 拆分并解析为 InetSocketAddress
            // 注意：此处 address 格式需与注册时保持一致
            String[] parts = address.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            return new InetSocketAddress(host, port);

        } catch (Exception e) {
            System.err.println("【ZK 发现失败】查询过程出现异常：" + e.getMessage());
            return null;
        }
    }
}