package com.jiejie.rpc.core.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

public class NettyClientHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        // 将服务端传回的结果暂存到 Channel 的属性中，以便 Client 主线程读取
        AttributeKey<Object> key = AttributeKey.valueOf("rpcResponse");
        ctx.channel().attr(key).set(msg);
        ctx.close();
    }
}