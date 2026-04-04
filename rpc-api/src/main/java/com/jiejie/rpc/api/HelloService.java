package com.jiejie.rpc.api;

/**
 * 远程服务契约接口。
 * 定义客户端与服务端共同遵循的业务方法标准，是 RPC 调用中的核心抽象。
 * * @author jiejie
 */
public interface HelloService {

    /**
     * 远程打招呼方法。
     * 用于验证 RPC 链路连通性、序列化效率及动态代理的拦截逻辑。
     *
     * @param object 封装业务请求数据的 {@link HelloObject} DTO 实例
     * @return 服务端处理后的响应字符串
     */
    String sayHello(HelloObject object);

}