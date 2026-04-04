package com.jiejie.rpc.api;

import java.io.Serializable;

/**
 * 测试用的实体类，用于在网络中传输
 */
public class HelloObject implements Serializable {
    // 建议加上 serialVersionUID，保证序列化版本的兼容性
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String message;

    // 空参构造（序列化框架底层通常需要空参构造，建议养成保留的习惯）
    public HelloObject() {
    }

    // 全参构造
    public HelloObject(Integer id, String message) {
        this.id = id;
        this.message = message;
    }

    // --- 下面是 Getter 和 Setter ---

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