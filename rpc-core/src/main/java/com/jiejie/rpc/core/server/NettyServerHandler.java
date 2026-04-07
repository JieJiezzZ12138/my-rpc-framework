package com.jiejie.rpc.core.server;

import com.jiejie.rpc.core.entity.RpcRequest;
import com.jiejie.rpc.core.provider.ServiceProvider;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.lang.reflect.Method;

/**
 * Netty 服务端核心业务处理器
 * 负责解析入站的 RpcRequest 请求，通过反射调用本地服务，并将执行结果异步写回客户端
 *
 * @author JieJie
 * @date 2026-04-07
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    /** 本地服务注册表：存储接口名与实现类实例的映射关系 */
    private final ServiceProvider serviceProvider;

    public NettyServerHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    /**
     * 读取并处理客户端发送的 RpcRequest 请求
     * * @param ctx Channel 处理上下文
     * @param msg 已解密/反序列化后的 RpcRequest 请求对象
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        // 1. 服务路由：根据请求中的接口全限定名，从本地容器中检索对应的服务实现类实例
        Object service = serviceProvider.getService(msg.getInterfaceName());

        // 2. 方法定位：利用反射，根据方法名和参数类型列表获取目标 Method 对象
        Method method = service.getClass().getMethod(msg.getMethodName(), msg.getParamTypes());

        // 3. 执行调用：通过反射触发本地业务逻辑，并获取返回值
        Object result = method.invoke(service, msg.getParameters());

        // 4. 响应回写：将结果序列化后写回 Channel
        // 特别注意：添加 CLOSE 监听器是为了在发送完毕后立即断开连接（当前版本采用短连接模式）
        ctx.writeAndFlush(result).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 异常捕获处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("【服务端 Handler】运行中捕捉到异常：");
        cause.printStackTrace();
        // 发生错误时主动关闭连接，防止死连接占用资源
        ctx.close();
    }
}