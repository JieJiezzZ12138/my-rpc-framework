package com.jiejie.rpc.core.entity;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RPC 请求协议报文实体 (V13.0 异步多路复用版)
 * <p>
 * 核心升级：新增 requestId 字段。
 * 在长连接复用场景下，用于匹配异步返回的响应包，实现非阻塞通讯。
 * </p>
 *
 * @author JieJie
 */
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    // 用于生成全局唯一的请求 ID
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    /** 请求唯一标识码 (V13.0 核心) */
    private long requestId;

    /** 目标接口的全限定名 */
    private String interfaceName;

    /** 目标方法名 */
    private String methodName;

    /** 实际调用的参数值 */
    private Object[] parameters;

    /** 参数的 Class 类型数组 */
    private Class<?>[] paramTypes;

    /**
     * 无参构造函数：兼容 Jackson 反序列化
     */
    public RpcRequest() {
    }

    /**
     * 全参构造函数：供客户端组装请求使用
     * 自动生成唯一的 requestId
     */
    public RpcRequest(String interfaceName, String methodName, Object[] parameters, Class<?>[] paramTypes) {
        this.requestId = ID_GENERATOR.incrementAndGet();
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameters = parameters;
        this.paramTypes = paramTypes;
    }

    // ==========================================
    // Getters & Setters
    // ==========================================
    public long getRequestId() { return requestId; }
    public void setRequestId(long requestId) { this.requestId = requestId; }

    public String getInterfaceName() { return interfaceName; }
    public void setInterfaceName(String interfaceName) { this.interfaceName = interfaceName; }

    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }

    public Object[] getParameters() { return parameters; }
    public void setParameters(Object[] parameters) { this.parameters = parameters; }

    public Class<?>[] getParamTypes() { return paramTypes; }
    public void setParamTypes(Class<?>[] paramTypes) { this.paramTypes = paramTypes; }
}