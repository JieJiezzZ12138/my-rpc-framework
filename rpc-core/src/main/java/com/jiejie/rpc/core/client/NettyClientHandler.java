package com.jiejie.rpc.core.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

/**
 * Netty 客户端出站处理器
 * 负责接收服务端响应数据，并将其挂载到当前 Channel 上供调用方读取
 *
 * @author JieJie
 * @date 2026-04-07
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<Object> {

    /**
     * 读取服务端写回的数据
     * 使用 AttributeKey 将异步获取的结果暂存至 Channel 属性，以解决 Netty 异步通信结果获取问题
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        // 1. 定义结果暂存的 Key（需与 RpcClient 中读取时的 Key 保持一致）
        AttributeKey<Object> key = AttributeKey.valueOf("rpcResponse");

        // 2. 将服务端传回的 RpcResponse（或结果对象）存入 Channel 上下文
        ctx.channel().attr(key).set(msg);

        // 3. 读取完毕后主动关闭连接（当前 V6.0 为短连接模式）
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("【客户端处理器】发生异常：" + cause.getMessage());
        ctx.close();
    }
}