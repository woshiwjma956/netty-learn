package com.ycorn.netty.tcpprotocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-09 16:35
 */
public class TCPProtocolEncoder extends MessageToByteEncoder<MessageProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageProtocol msg, ByteBuf out) throws Exception {
        int len = msg.getLen();
        out.writeInt(len);
        out.writeBytes(msg.getContent());
    }
}