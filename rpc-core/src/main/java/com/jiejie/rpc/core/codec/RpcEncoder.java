package com.jiejie.rpc.core.codec;

import com.jiejie.rpc.core.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 自定义 RPC 通信协议编码器 (Netty 出站管道组件)
 * <p>
 * 痛点背景：TCP 协议底层是无边界的字节流（水管模式），高并发下极易出现“粘包”现象。
 * 协议设计：采用经典的 [定长报文头 + 变长报文体] 封包协议。
 * <br>
 * 数据包结构：
 * +----------------+-----------------------+
 * | Length(4 Byte) | Payload(Actual Bytes) |
 * +----------------+-----------------------+
 * </p>
 *
 * @author JieJie
 * @date 2026-04-08
 */
public class RpcEncoder extends MessageToByteEncoder<Object> {

    // 通过 SPI 动态注入的序列化引擎（如 JSON, Kryo）
    private final Serializer serializer;

    public RpcEncoder(Serializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 将 Java 对象编码为自定义协议格式的字节流。
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        // 1. 调用底层的 SPI 序列化引擎，将对象转化为字节数组
        byte[] payloadBytes = serializer.serialize(msg);

        // 2. 写入 Header：将数据包的实际长度写入缓冲区 (int 类型严格占用 4 个字节)
        out.writeInt(payloadBytes.length);

        // 3. 写入 Body：将真正的序列化数据排在长度之后发出去
        out.writeBytes(payloadBytes);
    }
}