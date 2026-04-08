package com.jiejie.rpc.core.serialize.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.jiejie.rpc.core.serialize.Serializer;
import com.jiejie.rpc.core.serialize.SerializerCode;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * 基于 Kryo 的高性能序列化实现 (V14.0)
 */
public class KryoSerializer implements Serializer {

    /**
     * Kryo 实例是非线程安全的，使用 ThreadLocal 保证线程安全
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 设置不需要提前注册类（如果不设置，每个要传输的类都得显式 register）
        kryo.setRegistrationRequired(false);
        // 开启循环引用检测，防止无限递归
        kryo.setReferences(true);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        // Output 和 Input 也要用 try-with-resources 自动关闭
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Output output = new Output(baos)) {
            Kryo kryo = kryoThreadLocal.get();
            // 写入对象
            kryo.writeClassAndObject(output, obj);
            return output.toBytes();
        } catch (Exception e) {
            throw new RuntimeException("【Kryo】序列化失败：" + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             Input input = new Input(bais)) {
            Kryo kryo = kryoThreadLocal.get();
            // 读取对象
            return (T) kryo.readClassAndObject(input);
        } catch (Exception e) {
            throw new RuntimeException("【Kryo】反序列化失败：" + e.getMessage());
        }
    }

    @Override
    public byte getSerializerCode() {
        return SerializerCode.KRYO.getCode(); // 返回标识码 2
    }

    @Override
    public String getSerializerName() {
        return "kryo";
    }
}