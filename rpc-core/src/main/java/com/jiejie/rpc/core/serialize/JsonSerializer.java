package com.jiejie.rpc.core.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 基于 Jackson 的 JSON 序列化组件 (框架默认提供)
 * <p>
 * 优势：拥有极佳的跨语言兼容性，报文可读性强，方便网络抓包调试。
 * 提示：在此版本中为保证线程安全，每次反序列化未做极其深度的复用优化；
 * 在生产级极高并发场景中，可进一步针对 ObjectMapper 的特征做深度定制。
 * </p>
 *
 * @author JieJie
 * @date 2026-04-08
 */
public class JsonSerializer implements Serializer {

    // Jackson 核心引擎，默认是线程安全的，适合作为全局单例复用
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getSerializerName() {
        return "json";
    }

    @Override
    public byte[] serialize(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            // 抛出明确的运行时异常，阻断非法的网络传输
            throw new RuntimeException("【JSON 序列化严重异常】对象转换为字节流失败: " + e.getMessage());
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new RuntimeException("【JSON 反序列化严重异常】字节流还原对象失败: " + e.getMessage());
        }
    }
}