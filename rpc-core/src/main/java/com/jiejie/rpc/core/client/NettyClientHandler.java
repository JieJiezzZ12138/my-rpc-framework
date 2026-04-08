package com.jiejie.rpc.core.client;

import com.jiejie.rpc.core.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

/**
 * Netty 客户端核心业务处理器 (V8.0 信封解析版)
 * <p>
 * 负责接收服务端的 RpcResponse 响应，剥开信封校验状态，
 * 并将真实的数据挂载到当前 Channel 上供调用方读取。
 * </p>
 *
 * @author JieJie
 * @date 2026-04-08
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    /**
     * 读取服务端写回的数据 (此时的 msg 已经是经过解码器反序列化后的 RpcResponse 对象)
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
        // 1. 定义结果暂存的 Key（需与 NettyRpcClient 中读取时的 Key 保持一致）
        AttributeKey<Object> key = AttributeKey.valueOf("rpcResponse");

        // 2. 拆信封：校验服务端的执行状态
        if (response.getCode() != null && response.getCode() == 200) {
            // 业务成功：将信封里的真实数据 (Data) 暂存至 Channel 属性
            ctx.channel().attr(key).set(response.getData());
        } else {
            // 业务失败：打印服务端透传过来的异常提示，并存入 null (或者抛出自定义异常)
            System.err.println("【客户端 Handler 告警】服务端业务执行失败，原因：" + response.getMessage());
            ctx.channel().attr(key).set(null);
        }

        // 3. 解除阻塞唤醒主线程
        // 【架构预警】：为了配合 NettyRpcClient 中的 channel.closeFuture().sync()，
        // 这里目前依然必须调用 close() 才能让主线程继续往下走。
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("【客户端 Handler】网络通信发生严重异常：" + cause.getMessage());
        ctx.close();
    }
}