package com.jiejie.rpc.core.client;

import com.jiejie.rpc.core.entity.RpcRequest;
import com.jiejie.rpc.core.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

/**
 * Netty 客户端业务处理器 (V11.0 心跳保活与自动重连版)
 * <p>
 * 职责：
 * 1. 解析 RpcResponse 响应结果。
 * 2. 监听空闲状态事件，主动向服务端发送心跳包 (V9.0)。
 * </p>
 *
 * @author JieJie
 * @date 2026-04-08
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
        AttributeKey<Object> key = AttributeKey.valueOf("rpcResponse");

        if (response.getCode() != null && response.getCode() == 200) {
            ctx.channel().attr(key).set(response.getData());
        } else {
            System.err.println("【客户端】服务端返回异常：" + response.getMessage());
            ctx.channel().attr(key).set(null);
        }

        // 目前仍保留 close 以唤醒 NettyRpcClient 中的 closeFuture.sync()
        // 后续引入 CompletableFuture 后，这里将不再 close 而是直接 complete
        ctx.close();
    }

    /**
     * 【V9.0 核心】：处理心跳事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                // 10秒没写数据了，发个心跳包过去
                System.out.println("【客户端】检测到写空闲，正在发送心跳包维持连接...");
                // 构造一个特殊的心跳请求（methodName 设为 heartbeat）
                RpcRequest heartbeat = new RpcRequest();
                heartbeat.setMethodName("heartbeat");
                ctx.writeAndFlush(heartbeat);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("【客户端】网络异常：" + cause.getMessage());
        ctx.close();
    }
}