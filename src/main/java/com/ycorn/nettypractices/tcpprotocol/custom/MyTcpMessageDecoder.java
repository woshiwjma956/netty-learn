package com.ycorn.nettypractices.tcpprotocol.custom;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/7/3 16:04
 */

public class MyTcpMessageDecoder extends ByteToMessageDecoder {

    // 更消息安全的解码器参考https://www.cnblogs.com/sidesky/p/6913109.html
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readInt();
        MyTcpMessage msg = new MyTcpMessage();
        msg.setLength(length);
        byte[] data = new byte[length];
        ByteBuf buf = in.readBytes(data);
        msg.setContent(data);
        out.add(msg);
    }
}
