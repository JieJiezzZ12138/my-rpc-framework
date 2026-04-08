package com.jiejie.rpc.core.serialize;

import java.util.ServiceLoader;

/**
 * 序列化器工厂 (V14.0 SPI 增强版)
 */
public class SpiSerializerFactory {

    /**
     * 获取序列化器 (支持动态匹配)
     */
    public static Serializer getSerializer() {
        // 1. 这里暂时写死为 "kryo" 进行测试
        // 后面你可以从 rpc.properties 读这个字符串
        String targetName = "kryo";

        // 2. 加载所有 Serializer 接口的实现类
        ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);

        // 3. 遍历找到名字匹配的那一个
        for (Serializer serializer : serviceLoader) {
            if (serializer.getSerializerName().equalsIgnoreCase(targetName)) {
                System.out.println("【SPI 成功】已加载序列化器：" + serializer.getSerializerName());
                return serializer;
            }
        }

        // 4. 没找到就报个错
        throw new RuntimeException("【SPI 错误】未找到名为 " + targetName + " 的序列化实现！");
    }
}