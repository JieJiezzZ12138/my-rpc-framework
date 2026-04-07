package com.jiejie.rpc.core.client;

import com.jiejie.rpc.core.entity.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.AttributeKey;

/**
 * 基于 Netty 实现的 NIO 客户端。
 */
public class NettyRpcClient implements RpcClient {

    private final String host;
    private final int port;
    private static final EventLoopGroup group = new NioEventLoopGroup();

    public NettyRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ObjectEncoder());
                            ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            ch.pipeline().addLast(new NettyClientHandler());
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            Channel channel = future.channel();

            // 发送请求
            channel.writeAndFlush(rpcRequest).sync();
            // 阻塞等待响应结果（后续 V7.0 会改为异步 Future）
            channel.closeFuture().sync();

            // 从 Channel 的属性中取出 Handler 存入的结果
            AttributeKey<Object> key = AttributeKey.valueOf("rpcResponse");
            return channel.attr(key).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}