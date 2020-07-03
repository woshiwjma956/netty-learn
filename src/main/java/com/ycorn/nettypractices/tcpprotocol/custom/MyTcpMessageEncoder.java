package com.ycorn.nettypractices.tcpprotocol.custom;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/7/3 16:01
 */

public class MyTcpMessageEncoder extends MessageToByteEncoder<MyTcpMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, MyTcpMessage msg, ByteBuf out) throws Exception {
        // 写入长度
        out.writeInt(msg.getLength());
        // 写入具体的数据
        out.writeBytes(msg.getContent());
    }
}
