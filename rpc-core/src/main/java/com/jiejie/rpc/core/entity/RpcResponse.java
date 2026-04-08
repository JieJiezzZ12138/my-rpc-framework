package com.jiejie.rpc.core.entity;

import java.io.Serializable;

/**
 * RPC 响应协议报文实体 (V8.0 标准信封)
 * <p>
 * 统一封装服务端的返回结果，解决 JSON 序列化丢失类型的问题，
 * 并支持向客户端透传服务端的业务异常或执行状态。
 * </p>
 *
 * @author JieJie
 */
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 响应状态码 (例如：200 成功，500 失败) */
    private Integer code;
    /** 响应提示信息 (通常用于携带服务端的异常报错信息) */
    private String message;
    /** 实际的业务返回数据 (可能是 String, User 对象等) */
    private Object data;

    // ==========================================
    // 【必须】无参构造函数，专供 Jackson 等反射框架使用
    // ==========================================
    public RpcResponse() {
    }

    // ==========================================
    // 静态工厂方法，方便服务端快速构建响应信封
    // ==========================================
    public static RpcResponse success(Object data) {
        RpcResponse response = new RpcResponse();
        response.setCode(200);
        response.setMessage("RPC 调用成功");
        response.setData(data);
        return response;
    }

    public static RpcResponse fail(String message) {
        RpcResponse response = new RpcResponse();
        response.setCode(500);
        response.setMessage(message);
        return response;
    }

    // ==========================================
    // Getters & Setters
    // ==========================================
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}