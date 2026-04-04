package com.jiejie.rpc.api;

/**
 * 测试用的服务接口
 */
public interface HelloService {
    /**
     * 打招呼方法
     * @param object 传入的实体对象
     * @return 返回处理后的字符串
     */
    String sayHello(HelloObject object);
}