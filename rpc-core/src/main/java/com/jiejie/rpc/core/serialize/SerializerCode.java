package com.jiejie.rpc.core.serialize;

/**
 * 序列化算法标识码 (V14.0 无插件纯净版)
 */
public enum SerializerCode {
    JSON((byte) 1),
    KRYO((byte) 2);

    private final byte code;

    // 手写构造函数
    SerializerCode(byte code) {
        this.code = code;
    }

    // 手写 Getter，确保编译器一定能解析到 getCode()
    public byte getCode() {
        return code;
    }
}