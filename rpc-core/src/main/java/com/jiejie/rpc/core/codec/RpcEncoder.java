package com.jiejie.rpc.core.codec;

import com.jiejie.rpc.core.entity.ProtocolConstants;
import com.jiejie.rpc.core.entity.RpcRequest;
import com.jiejie.rpc.core.entity.RpcResponse;
import com.jiejie.rpc.core.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 自定义协议编码器 (V13.0 异步多路复用版)
 * 协议头扩容至 24 字节，新增 8 字节 Request ID 字段
 */
public class RpcEncoder extends MessageToByteEncoder<Object> {

    private final Serializer serializer;

    public RpcEncoder(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        // 1. 写入魔数 (4 bytes)
        out.writeBytes(ProtocolConstants.MAGIC_NUMBER);

        // 2. 写入版本号 (1 byte)
        out.writeByte(ProtocolConstants.VERSION);

        // 3. 写入序列化代码 (1 byte)
        out.writeByte(serializer.getSerializerCode());

        // 4. 写入消息类型 (1 byte)
        byte msgType = (msg instanceof RpcRequest) ? ProtocolConstants.REQUEST_TYPE : ProtocolConstants.RESPONSE_TYPE;
        out.writeByte(msgType);

        // 5. 写入状态码 (1 byte)
        out.writeByte(ProtocolConstants.STATUS_SUCCESS);

        // 6. 【V13.0 新增】写入 Request ID (8 bytes)
        // 从实体类中提取 ID 并写入 Header
        long requestId = (msg instanceof RpcRequest)
                ? ((RpcRequest) msg).getRequestId()
                : ((RpcResponse) msg).getRequestId();
        out.writeLong(requestId);

        // 7. 序列化业务 Body
        byte[] body = serializer.serialize(msg);

        // 8. 写入包体长度 (4 bytes)
        out.writeInt(body.length);

        // 9. 写入预留字段 (4 bytes)
        out.writeInt(0);

        // 10. 写入业务 Body 数据
        out.writeBytes(body);
    }
}