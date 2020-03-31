package com.ycorn.netty.tcpprotocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.List;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-09 16:30
 */
public class TCPProtocolDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int len = in.readInt();
        byte[] content = new byte[len];
        in.readBytes(content);
        MessageProtocol messageProtocol = new MessageProtocol();
        messageProtocol.setLen(len);
        messageProtocol.setContent(content);
        out.add(messageProtocol);
    }
}