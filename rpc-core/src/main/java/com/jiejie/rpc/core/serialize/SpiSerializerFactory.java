package com.jiejie.rpc.core.serialize;

import com.jiejie.rpc.core.util.PropertiesUtil;
import java.util.ServiceLoader;

/**
 * 序列化引擎 SPI 工厂
 */
public class SpiSerializerFactory {
    public static Serializer getSerializer() {
        // 读取配置，默认走 json
        String strategy = PropertiesUtil.getString("rpc.serializer.strategy", "json");

        ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);
        for (Serializer serializer : serviceLoader) {
            if (serializer.getSerializerName().equalsIgnoreCase(strategy)) {
                return serializer;
            }
        }

        // 兜底策略
        System.err.println("【SPI 告警】未找到名为 [" + strategy + "] 的序列化器，降级使用默认 JSON 策略。");
        return new JsonSerializer();
    }
}