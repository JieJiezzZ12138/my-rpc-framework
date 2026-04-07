package com.jiejie.rpc.core.client;

import com.jiejie.rpc.core.entity.RpcRequest;

/**
 * RPC 客户端通用传输接口
 * 定义了客户端发送请求的核心契约，具体的传输实现（如 Socket、Netty）需遵循此标准
 *
 * @author JieJie
 * @date 2026-04-07
 */
public interface RpcClient {

    /**
     * 发送 RPC 请求并同步获取远程方法的执行结果
     * * @param rpcRequest 封装了调用信息的请求报文实体
     * @return 远程服务端返回的业务处理结果对象
     */
    Object sendRequest(RpcRequest rpcRequest);
}