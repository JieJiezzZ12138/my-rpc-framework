package com.jiejie.rpc.api;

import java.io.Serializable;

/**
 * RPC 通信测试用的数据传输对象 (DTO)。
 * 实现了 {@link Serializable} 接口，确保对象可在网络间进行二进制流传输。
 * * @author jiejie
 */
public class HelloObject implements Serializable {

    /** 序列化版本标识，用于保障类结构在序列化与反序列化过程中的版本兼容性 */
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String message;

    /**
     * 默认无参构造器。
     * 许多序列化框架（如 Jackson, Hessian）在反序列化时通过反射调用此构造器实例化对象。
     */
    public HelloObject() {
    }

    /**
     * 全参构造器，用于快速初始化数据。
     *
     * @param id      业务标识 ID
     * @param message 测试消息内容
     */
    public HelloObject(Integer id, String message) {
        this.id = id;
        this.message = message;
    }

    // --- 标准的 Getter/Setter 方法 ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}