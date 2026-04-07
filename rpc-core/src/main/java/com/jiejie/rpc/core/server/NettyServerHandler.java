package com.jiejie.rpc.core.server;

import com.jiejie.rpc.core.entity.RpcRequest;
import com.jiejie.rpc.core.provider.ServiceProvider;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.lang.reflect.Method;

/**
 * 服务端 Netty 处理器。
 * 处理入站的 RpcRequest，并在执行后写回结果。
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final ServiceProvider serviceProvider;

    public NettyServerHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        // 1. 路由：从注册表获取实例
        Object service = serviceProvider.getService(msg.getInterfaceName());

        // 2. 反射：执行目标方法
        Method method = service.getClass().getMethod(msg.getMethodName(), msg.getParamTypes());
        Object result = method.invoke(service, msg.getParameters());

        // 3. 写回结果并关闭连接（当前短连接模式）
        ctx.writeAndFlush(result).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}