package com.jiejie.rpc.core.client;

import com.jiejie.rpc.core.entity.RpcRequest;
import com.jiejie.rpc.core.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Netty 客户端业务处理器 (V13.0 异步多路复用版)
 * <p>
 * 核心设计：
 * 1. 响应分发：利用 requestId 从全局 Map 中寻找对应的 CompletableFuture。
 * 2. 异步唤醒：通过 future.complete() 机制，实现 IO 线程对业务线程的非阻塞通知。
 * 3. 连接复用：读取数据后不再关闭通道，支持单条 TCP 链路上的并发交替请求。
 * </p>
 *
 * @author JieJie
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    /**
     * 收到服务端响应时的回调
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
        long requestId = response.getRequestId();

        // 【关键】从全局存根中移除并获取该请求的 Future
        // remove 是原子操作，确保在高并发下每个响应只被处理一次
        CompletableFuture<RpcResponse> future = NettyRpcClient.UNPROCESSED_REQUESTS.remove(requestId);

        if (future != null) {
            // 唤醒在 NettyRpcClient 中调用 get() 的业务线程
            future.complete(response);
            System.out.println("【客户端】成功匹配响应包，RequestId: " + requestId);
        } else {
            // 可能是因为客户端超时已经自行移除了该 ID，或者收到了错误的包
            System.err.println("【客户端】收到未识别或已超时的响应，RequestId: " + requestId);
        }

        // V13.0 严禁在此处调用 ctx.close()，否则多路复用长连接会失效
    }

    /**
     * 处理 Netty 事件（心跳、连接状态等）
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                System.out.println("【客户端】发送心跳包以维持长连接...");

                // 构造心跳包
                RpcRequest heartbeat = new RpcRequest();
                heartbeat.setMethodName("heartbeat");
                // 确保心跳包也有 ID，满足 24 字节 Encoder 的要求
                // (如果 RpcRequest 无参构造函数没设 ID，建议手动 set 一个 0)
                heartbeat.setRequestId(0L);

                ctx.writeAndFlush(heartbeat).addListener(f -> {
                    if (!f.isSuccess()) {
                        System.err.println("【客户端】心跳发送失败，准备断开连接重连");
                        // 直接使用当前的 ChannelHandlerContext 来关闭通道
                        ctx.channel().close();
                    }
                });
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 异常处理：在异步模式下，异常不仅要打印，还应该通知对应的 Future
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("【客户端】网络链路异常: " + cause.getMessage());
        // 发生异常时关闭连接，触发下一次请求时的重连逻辑
        ctx.close();
    }
}