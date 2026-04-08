package com.jiejie.rpc.core.serialize;

/**
 * 序列化接口 (V12.0 完整版)
 */
public interface Serializer {

    /** 序列化 */
    byte[] serialize(Object obj);

    /** 反序列化 */
    <T> T deserialize(byte[] bytes, Class<T> clazz);

    /** * 获取序列化器唯一标识码 (用于协议头传输)
     */
    byte getSerializerCode();

    /** * 获取序列化器名称 (用于 SPI 加载与日志打印)
     */
    String getSerializerName();
}