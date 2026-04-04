package com.jiejie.rpc.core.entity;

import java.io.Serializable;

/**
 * RPC 远程调用请求协议报文。
 * 封装了客户端发起远程过程调用所需的全部元数据，通过 {@link Serializable} 接口支持网络传输。
 * * @author jiejie
 */
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 目标接口的全限定名 */
    private String interfaceName;

    /** 目标方法名 */
    private String methodName;

    /** 调用方法的实际参数值序列 */
    private Object[] parameters;

    /** * 调用方法的参数类型序列。
     * 在 Java 反射机制中，需结合方法名与参数类型以精准定位存在重载（Overload）的目标方法。
     */
    private Class<?>[] paramTypes;

    /**
     * 默认构造器。
     * 用于反序列化框架在运行时通过反射实例化请求对象。
     */
    public RpcRequest() {}

    /**
     * 全参构造器。
     *
     * @param interfaceName 接口全限定名
     * @param methodName    方法名
     * @param parameters    参数对象数组
     * @param paramTypes    参数类型数组
     */
    public RpcRequest(String interfaceName, String methodName, Object[] parameters, Class<?>[] paramTypes) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameters = parameters;
        this.paramTypes = paramTypes;
    }

    /* Getter & Setter */

    public String getInterfaceName() { return interfaceName; }
    public void setInterfaceName(String interfaceName) { this.interfaceName = interfaceName; }

    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }

    public Object[] getParameters() { return parameters; }
    public void setParameters(Object[] parameters) { this.parameters = parameters; }

    public Class<?>[] getParamTypes() { return paramTypes; }
    public void setParamTypes(Class<?>[] paramTypes) { this.paramTypes = paramTypes; }
}