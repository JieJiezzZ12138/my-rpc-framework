package com.jiejie.rpc.core.entity;

import java.io.Serializable;

/**
 * RPC 响应协议报文实体 (V13.0 异步多路复用版)
 * <p>
 * 核心升级：新增 requestId，用于在异步通讯中精准匹配对应的请求。
 * 即使在高并发下，也能保证响应结果准确返回给对应的调用线程。
 * </p>
 *
 * @author JieJie
 */
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 响应对应的请求 ID (V13.0 核心：用于异步匹配) */
    private long requestId;

    /** 响应状态码 (例如：200 成功，500 失败) */
    private Integer code;

    /** 响应提示信息 */
    private String message;

    /** 实际的业务返回数据 */
    private Object data;

    /**
     * 【必须】无参构造函数：专供 Jackson 等反射框架反序列化使用
     */
    public RpcResponse() {
    }

    // ==========================================
    // 静态工厂方法：必须携带 requestId 以便客户端匹配
    // ==========================================

    /**
     * 快速构建成功响应
     * @param data 返回的业务数据
     * @param requestId 对应的请求 ID
     */
    public static RpcResponse success(Object data, long requestId) {
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setCode(200);
        response.setMessage("RPC 调用成功");
        response.setData(data);
        return response;
    }

    /**
     * 快速构建失败响应
     * @param message 错误信息
     * @param requestId 对应的请求 ID
     */
    public static RpcResponse fail(String message, long requestId) {
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setCode(500);
        response.setMessage(message);
        return response;
    }

    // ==========================================
    // Getters & Setters
    // ==========================================
    public long getRequestId() { return requestId; }
    public void setRequestId(long requestId) { this.requestId = requestId; }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}