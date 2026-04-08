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
 * Netty 服务端核心业务处理器 (V13.0 异步多路复用版)
 * <p>
 * 核心升级：
 * 1. 响应回执：在 RpcResponse 中强制携带 requestId，支撑客户端的异步多路复用。
 * 2. 状态对齐：适配 V12.0/V13.0 的静态工厂方法。
 * 3. 类型兼容：保留 Jackson convertValue 逻辑，解决 JSON 反序列化后的类型擦除痛点。
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
        // 1. 提取请求 ID (V13.0 的核心“回执单号”)
        long requestId = msg.getRequestId();

        // 2. 【V9.0 核心逻辑】：拦截心跳包
        if ("heartbeat".equals(msg.getMethodName())) {
            // 注意：心跳包不需要执行业务，直接返回即可，IdleStateHandler 会自动重置读感应
            return;
        }

        System.out.println("【服务端】收到业务请求，ID: " + requestId + "，方法: " + msg.getMethodName());

        RpcResponse response;
        try {
            // 3. 获取服务实例 (从注册中心/ServiceProvider)
            Object service = serviceProvider.getService(msg.getInterfaceName());
            if (service == null) {
                throw new RuntimeException("服务端未找到服务实例: " + msg.getInterfaceName());
            }

            // 4. 获取方法对象
            Method method = service.getClass().getMethod(msg.getMethodName(), msg.getParamTypes());

            // 5. 【V8.0 核心魔法】：Jackson 类型二次转换
            // 解决 JSON 反序列化后数值类型变位 Integer/LinkedHashMap 的问题
            Object[] parameters = msg.getParameters();
            Class<?>[] paramTypes = msg.getParamTypes();
            if (parameters != null && paramTypes != null && parameters.length == paramTypes.length) {
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = mapper.convertValue(parameters[i], paramTypes[i]);
                }
            }

            // 6. 反射调用
            Object result = method.invoke(service, parameters);

            // 7. 【V13.0 关键】：封装成功响应，必须透传 requestId
            response = RpcResponse.success(result, requestId);

        } catch (Exception e) {
            System.err.println("【服务端 Handler】业务方法执行失败，ID: " + requestId + "，原因：" + e.getMessage());
            // 8. 【V13.0 关键】：封装失败响应，也必须透传 requestId
            response = RpcResponse.fail("服务端逻辑执行失败: " + e.getMessage(), requestId);
        }

        // 9. 将响应回发给客户端 (经由 RpcEncoder 封装成 24 字节 Header 包)
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
                System.err.println("【服务端】检测到读空闲，长时间未收到心跳，主动关闭连接以节省资源。");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 异常捕捉处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("【服务端 Handler】网络通信发生致命异常，连接将强制关闭。");
        cause.printStackTrace();
        ctx.close();
    }
}