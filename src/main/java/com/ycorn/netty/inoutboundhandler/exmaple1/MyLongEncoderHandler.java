package com.ycorn.netty.inoutboundhandler.exmaple1;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-09 11:38
 */
public class MyLongEncoderHandler extends MessageToByteEncoder<Long> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Long msg, ByteBuf out) throws Exception {
        System.out.println("MyLongEncoderHandler.encode ....");
        out.writeLong(msg);
    }
}