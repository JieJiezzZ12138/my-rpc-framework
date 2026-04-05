package com.jiejie.rpc.core.entity;

import java.io.Serializable;

/**
 * RPC 请求协议报文 (V4.0)。
 * 封装了接口全限定名、方法名及参数元数据，支撑服务端实现动态路由分发。
 * * @author jiejie
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

    public RpcRequest(String interfaceName, String methodName, Object[] parameters, Class<?>[] paramTypes) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameters = parameters;
        this.paramTypes = paramTypes;
    }

    public String getInterfaceName() { return interfaceName; }
    public String getMethodName() { return methodName; }
    public Object[] getParameters() { return parameters; }
    public Class<?>[] getParamTypes() { return paramTypes; }
}