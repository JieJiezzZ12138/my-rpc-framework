package com.jiejie.rpc.core.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiejie.rpc.core.entity.RpcRequest;
import com.jiejie.rpc.core.entity.RpcResponse;
import com.jiejie.rpc.core.provider.ServiceProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

/**
 * Netty 服务端核心业务处理器 (V8.0 最终破解版)
 * <p>
 * 引入 Jackson 的 convertValue 魔法，彻底破解 JSON 反序列化过程中的 LinkedHashMap 类型擦除难题。
 * </p>
 *
 * @author JieJie
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final ServiceProvider serviceProvider;
    // 复用 ObjectMapper 提升性能
    private static final ObjectMapper mapper = new ObjectMapper();

    public NettyServerHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        RpcResponse response;
        try {
            Object service = serviceProvider.getService(msg.getInterfaceName());
            if (service == null) {
                throw new RuntimeException("服务端未找到服务实例: " + msg.getInterfaceName());
            }

            Method method = service.getClass().getMethod(msg.getMethodName(), msg.getParamTypes());

            Object[] parameters = msg.getParameters();
            Class<?>[] paramTypes = msg.getParamTypes();

            if (parameters != null && paramTypes != null && parameters.length == paramTypes.length) {
                for (int i = 0; i < parameters.length; i++) {
                    // 如果 Jackson 把对象错误地解析成了 LinkedHashMap，
                    // 利用 convertValue 和真实的 Class 类型，把它强行转换回原本的业务对象！
                    parameters[i] = mapper.convertValue(parameters[i], paramTypes[i]);
                }
            }
            // ==========================================

            // 参数纠正完毕，安全执行反射调用
            Object result = method.invoke(service, parameters);
            response = RpcResponse.success(result);

        } catch (Exception e) {
            System.err.println("【服务端 Handler】业务方法执行失败：" + e.getMessage());
            response = RpcResponse.fail("服务端逻辑失败: " + e.getMessage());
        }

        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("【服务端 Handler】网络通信发生异常：");
        cause.printStackTrace();
        ctx.close();
    }
}