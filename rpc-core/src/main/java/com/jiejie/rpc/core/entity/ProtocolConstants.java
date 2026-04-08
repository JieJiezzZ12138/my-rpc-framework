package com.jiejie.rpc.core.entity;

/**
 * RPC 协议常量定义 (V13.0 异步多路复用版)
 * <p>
 * 协议头总长：24 字节
 * 结构：魔数(4B) + 版本(1B) + 序列化(1B) + 类型(1B) + 状态(1B) + RequestId(8B) + 长度(4B) + 预留(4B)
 * </p>
 *
 * @author jiejie
 */
public class ProtocolConstants {

    /** 魔数，用于鉴权与安全校验，标识该请求属于本框架 */
    public static final byte[] MAGIC_NUMBER = {(byte) 'j', (byte) 'r', (byte) 'p', (byte) 'c'};

    /** 协议版本号 */
    public static final byte VERSION = 1;

    /** 消息类型：请求 */
    public static final byte REQUEST_TYPE = 1;
    /** 消息类型：响应 */
    public static final byte RESPONSE_TYPE = 2;
    /** 消息类型：心跳检测 */
    public static final byte HEARTBEAT_TYPE = 3;

    /** * 状态码：成功 (200)
     */
    public static final byte STATUS_SUCCESS = (byte) 200;

    /** * 状态码：服务端错误 (500)
     */
    public static final byte STATUS_FAIL = (byte) 500;

    /** * 【V13.0 核心】协议头固定总长度：24 字节
     * 必须与 RpcEncoder/RpcDecoder 中的读写逻辑严格对齐
     */
    public static final int HEADER_LENGTH = 24;
}