package com.jiejie.rpc.core.serialize.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiejie.rpc.core.serialize.Serializer;
import com.jiejie.rpc.core.serialize.SerializerCode;

import java.io.IOException;

/**
 * JSON 序列化处理器 (V12.0 完整版)
 * 实现了 Serializer 接口定义的所有“合同条款”
 */
public class JsonSerializer implements Serializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("【序列化异常】" + e.getMessage());
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new RuntimeException("【反序列化异常】" + e.getMessage());
        }
    }

    /**
     * 实现接口方法：返回序列化器的唯一标识码 (用于协议头传输)
     */
    @Override
    public byte getSerializerCode() {
        return SerializerCode.JSON.getCode();
    }

    /**
     * 实现接口方法：返回序列化器的名称 (用于 SPI 加载与日志)
     */
    @Override
    public String getSerializerName() {
        return "json";
    }
}