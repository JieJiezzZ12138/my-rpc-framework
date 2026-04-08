package com.jiejie.rpc.core.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * 核心配置读取工具类
 * 负责在框架启动时加载 rpc.properties 配置文件
 *
 * @author JieJie
 * @date 2026-04-08
 */
public class PropertiesUtil {

    private static final Properties properties = new Properties();

    // 静态代码块：类加载时自动执行，且只执行一次
    static {
        try (InputStream is = PropertiesUtil.class.getResourceAsStream("/rpc.properties")) {
            if (is != null) {
                properties.load(is);
                System.out.println("【全局配置】成功加载 rpc.properties 配置文件！");
            } else {
                System.err.println("【全局配置】未找到 rpc.properties，将使用框架默认配置。");
            }
        } catch (Exception e) {
            System.err.println("【全局配置】读取配置文件失败：" + e.getMessage());
        }
    }

    /**
     * 根据 Key 获取字符串属性
     * @param key 属性名
     * @param defaultValue 如果没配置该属性，返回的默认值
     */
    public static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}