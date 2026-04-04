package com.jiejie.rpc.core.entity;

import java.io.Serializable;

/**
 * RPC 请求实体类：封装了客户端想要调用的具体信息
 */
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 接口名称，例如：com.jiejie.rpc.api.HelloService */
    private String interfaceName;

    /** 方法名称，例如：sayHello */
    private String methodName;

    /** 方法的参数值列表 */
    private Object[] parameters;

    /** 方法的参数类型列表 */
    private Class<?>[] paramTypes;

    // --- 快捷键生成的空参、全参构造，以及 Getter 和 Setter ---

    public RpcRequest() {}

    public RpcRequest(String interfaceName, String methodName, Object[] parameters, Class<?>[] paramTypes) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameters = parameters;
        this.paramTypes = paramTypes;
    }

    public String getInterfaceName() { return interfaceName; }
    public void setInterfaceName(String interfaceName) { this.interfaceName = interfaceName; }

    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }

    public Object[] getParameters() { return parameters; }
    public void setParameters(Object[] parameters) { this.parameters = parameters; }

    public Class<?>[] getParamTypes() { return paramTypes; }
    public void setParamTypes(Class<?>[] paramTypes) { this.paramTypes = paramTypes; }
}