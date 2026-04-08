package com.jiejie.rpc.core.entity;

import java.io.Serializable;

/**
 * RPC 请求协议报文实体 (V8.0 序列化重构版)
 * <p>
 * 封装了接口全限定名、方法名及参数元数据，支撑服务端实现动态路由分发。
 * 新增了无参构造函数与 Setter 方法，以完美兼容 Jackson 等基于反射的 JSON 序列化框架。
 * </p>
 *
 * @author JieJie
 */
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 目标接口的全限定名，用于服务端在注册中心检索实例 */
    private String interfaceName;

    /** 目标方法名 */
    private String methodName;

    /** 实际调用的参数值 */
    private Object[] parameters;

    /** 参数的 Class 类型数组，用于反射时精准匹配重载方法 */
    private Class<?>[] paramTypes;

    // ==========================================
    // 【V8.0 核心修复】：必须提供的无参构造函数！
    // Jackson 等框架在反序列化时，会先调用此构造器创建一个“空壳”对象，
    // 然后再通过反射或 Setter 方法把解析出来的 JSON 值塞进去。
    // ==========================================
    public RpcRequest() {
    }

    // 原本的全参构造函数保留，方便客户端组装请求时使用
    public RpcRequest(String interfaceName, String methodName, Object[] parameters, Class<?>[] paramTypes) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameters = parameters;
        this.paramTypes = paramTypes;
    }

    // ==========================================
    // Getters & Setters
    // ==========================================
    public String getInterfaceName() { return interfaceName; }
    public void setInterfaceName(String interfaceName) { this.interfaceName = interfaceName; }

    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }

    public Object[] getParameters() { return parameters; }
    public void setParameters(Object[] parameters) { this.parameters = parameters; }

    public Class<?>[] getParamTypes() { return paramTypes; }
    public void setParamTypes(Class<?>[] paramTypes) { this.paramTypes = paramTypes; }
}