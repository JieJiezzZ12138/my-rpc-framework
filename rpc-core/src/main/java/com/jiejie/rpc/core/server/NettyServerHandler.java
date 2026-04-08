package com.jiejie.rpc.core.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiejie.rpc.core.entity.RpcRequest;
import com.jiejie.rpc.core.entity.RpcResponse;
import com.jiejie.rpc.core.provider.ServiceProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.lang.reflect.Method;

/**
 * Netty 服务端核心业务处理器 (V11.0 高可用心跳版)
 * <p>
 * 升级点：
 * 1. 增加对心跳包的识别与拦截 (V9.0)。
 * 2. 增加空闲检测处理，主动剔除失效客户端。
 * 3. 保留 Jackson convertValue 魔法，解决类型擦除问题。
 * </p>
 *
 * @author JieJie
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final ServiceProvider serviceProvider;
    private static final ObjectMapper mapper = new ObjectMapper();

    public NettyServerHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        // 【V9.0 核心逻辑】：过滤心跳包
        // 如果方法名是 heartbeat，说明是客户端发送的保活报文，直接忽略，IdleStateHandler 会自动重置计时
        if ("heartbeat".equals(msg.getMethodName())) {
            return;
        }

        System.out.println("【服务端】收到业务请求: " + msg.getMethodName());
        RpcResponse response;
        try {
            // 1. 获取服务实例
            Object service = serviceProvider.getService(msg.getInterfaceName());
            if (service == null) {
                throw new RuntimeException("服务端未找到服务实例: " + msg.getInterfaceName());
            }

            // 2. 获取方法对象
            Method method = service.getClass().getMethod(msg.getMethodName(), msg.getParamTypes());

            // 3. 【V8.0 遗留魔法】：类型二次转换
            Object[] parameters = msg.getParameters();
            Class<?>[] paramTypes = msg.getParamTypes();
            if (parameters != null && paramTypes != null && parameters.length == paramTypes.length) {
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = mapper.convertValue(parameters[i], paramTypes[i]);
                }
            }

            // 4. 反射调用并封装结果
            Object result = method.invoke(service, parameters);
            response = RpcResponse.success(result);

        } catch (Exception e) {
            System.err.println("【服务端 Handler】业务方法执行失败：" + e.getMessage());
            response = RpcResponse.fail("服务端逻辑执行失败: " + e.getMessage());
        }

        // 5. 将 RpcResponse 发回客户端
        ctx.writeAndFlush(response);
    }

    /**
     * 【V9.0/V11.0 核心】：处理 Netty 空闲状态事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                // 如果 30 秒没有收到读请求，说明连接已失效
                System.err.println("【服务端】检测到读空闲，长时间未收到心跳，主动关闭连接。");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("【服务端 Handler】网络通信异常：");
        cause.printStackTrace();
        ctx.close();
    }
}