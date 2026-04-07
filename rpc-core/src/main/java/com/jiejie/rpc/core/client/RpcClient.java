package com.jiejie.rpc.core.client;

import com.jiejie.rpc.core.entity.RpcRequest;

/**
 * RPC 客户端通用接口。
 * * @author jiejie
 */
public interface RpcClient {
    /**
     * 发送请求并获取结果
     */
    Object sendRequest(RpcRequest rpcRequest);
}