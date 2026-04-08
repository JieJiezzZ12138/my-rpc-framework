package com.jiejie.rpc.core.serialize;

/**
 * 序列化引擎通用接口 (V8.0 高性能通信架构 SPI 扩展点)
 * <p>
 * 核心职责：将 Java 业务对象与网络传输的字节数组进行相互转换，替代低效且危险的 Java 原生序列化。
 * 架构设计：基于微内核 SPI 机制设计，框架使用者可在不修改此核心源码的前提下，
 * 通过 META-INF/services 无缝热插拔自定义的高效序列化算法（如 Kryo, Protobuf, Hessian 等）。
 * </p>
 *
 * @author JieJie
 * @date 2026-04-08
 */
public interface Serializer {

    /**
     * 获取当前序列化算法的唯一标识名称。
     * <p>结合 properties 配置文件，用于在启动时动态路由定位到具体的实现类。</p>
     *
     * @return 序列化器名称标识 (例如: "json", "kryo")
     */
    String getSerializerName();

    /**
     * 将业务对象序列化为字节数组 (出站前置操作)。
     *
     * @param obj 需要传输的任意业务对象 (Request / Response)
     * @return 序列化后的字节数组，作为 TCP 报文的 Payload
     */
    byte[] serialize(Object obj);

    /**
     * 将接收到的字节数组反序列化为指定的 Java 对象 (入站后置操作)。
     *
     * @param bytes 网络层解析出的完整报文体 (Payload) 数据
     * @param clazz 目标对象的 Class 类型
     * @param <T>   目标类型泛型
     * @return 还原后的 Java 业务对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}