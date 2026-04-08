package com.jiejie.rpc.core.codec;

import com.jiejie.rpc.core.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 自定义 RPC 通信协议解码器 (Netty 入站管道组件)
 * <p>
 * 核心职责：不仅负责数据的反序列化，更核心的是解决 TCP 的“粘包”与“半包”问题。
 * 运行机制：继承自 {@link ByteToMessageDecoder}，它内部维护了一个字节累加器。
 * 当网络包被切片传输（半包）时，它会暂存数据并等待；当网络包连在一起（粘包）时，它会精准切割。
 * </p>
 *
 * @author JieJie
 * @date 2026-04-08
 */
public class RpcDecoder extends ByteToMessageDecoder {

    // 目标反序列化的类型 (客户端接收 Response，服务端接收 Request)
    private final Class<?> targetClass;

    // 通过 SPI 动态注入的反序列化引擎
    private final Serializer serializer;

    public RpcDecoder(Class<?> targetClass, Serializer serializer) {
        this.targetClass = targetClass;
        this.serializer = serializer;
    }

    /**
     * 将持续接收到的 TCP 字节流安全地拆解还原为 Java 对象。
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 1. 【安全防线】：报文头固定为 4 个字节(int)。
        // 如果当前缓冲区连报文头都凑不齐，说明是个极其严重的“半包”，直接返回，等待下一次 TCP 数据包到达
        if (in.readableBytes() < 4) {
            return;
        }

        // 2. 标记当前的读取位置指针 (Mark)
        // 作用：一旦发现后面的 Payload 数据还没传完，随时可以把指针回滚到这个位置
        in.markReaderIndex();

        // 3. 读取报文头中标记的实际数据长度 (读取并使内部指针向后移动 4 字节)
        int payloadLength = in.readInt();

        // 4. 【半包处理核心逻辑】：检查当前缓冲区剩余的数据量，够不够一个完整的 Payload
        if (in.readableBytes() < payloadLength) {
            // 发生了“半包”现象，数据还没接收完。
            // 将读取指针重置到刚才 mark 的地方（即回滚刚才的 readInt 操作），静静等待数据堆积完毕
            in.resetReaderIndex();
            return;
        }

        // 5. 数据完整！分配一个刚好容纳 Payload 的空字节数组
        byte[] payloadBytes = new byte[payloadLength];

        // 6. 将 Netty 缓冲区的数据抽取到我们自己的字节数组中
        in.readBytes(payloadBytes);

        // 7. 调用 SPI 反序列化引擎，将纯净的字节数组转化为业务对象
        Object obj = serializer.deserialize(payloadBytes, targetClass);

        // 8. 将解析完的完整对象丢入 out 集合，它会自动被传递给管道中的下一个业务 Handler
        out.add(obj);
    }
}