package com.jiejie.rpc.core.codec;

import com.jiejie.rpc.core.entity.ProtocolConstants;
import com.jiejie.rpc.core.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.Arrays;
import java.util.List;

/**
 * 自定义协议解码器 (V13.0 异步多路复用版)
 * 严格按照 24 字节 Header 结构解析二进制流
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private final Class<?> targetClass;
    private final Serializer serializer;

    public RpcDecoder(Class<?> targetClass, Serializer serializer) {
        this.targetClass = targetClass;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 1. 【V13.0 升级】基础校验：Header 至少要 24 字节
        if (in.readableBytes() < 24) return;

        in.markReaderIndex();

        // 2. 安全校验：验证魔数 (4 bytes)
        byte[] magic = new byte[4];
        in.readBytes(magic);
        if (!Arrays.equals(magic, ProtocolConstants.MAGIC_NUMBER)) {
            throw new IllegalArgumentException("【协议错误】非法的魔数标识！");
        }

        // 3. 读取元数据 (4 bytes)
        byte version = in.readByte();
        byte serializeCode = in.readByte();
        byte msgType = in.readByte();
        byte status = in.readByte();

        // 4. 【V13.0 新增】读取 Request ID (8 bytes)
        // 这一步虽然在反序列化 Body 时也能拿到 ID，但在 Header 里读取是为了后续做“零拷贝”或“快速分发”预留能力
        long requestId = in.readLong();

        // 5. 读取消息体长度 (4 bytes)
        int bodyLength = in.readInt();

        // 6. 跳过 4 字节预留位 (4 bytes)
        in.skipBytes(4);

        // 7. 完整性校验：半包处理
        if (in.readableBytes() < bodyLength) {
            in.resetReaderIndex();
            return;
        }

        // 8. 提取并反序列化 Body
        byte[] body = new byte[bodyLength];
        in.readBytes(body);
        Object obj = serializer.deserialize(body, targetClass);

        out.add(obj);
    }
}