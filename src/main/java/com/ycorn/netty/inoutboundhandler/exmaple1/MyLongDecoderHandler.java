package com.ycorn.netty.inoutboundhandler.exmaple1;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.List;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-09 11:32
 */
public class MyLongDecoderHandler extends ByteToMessageDecoder {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("MyLongDecoderHandler#decode ... ");
        // long 是8个字节
        if (in.readableBytes() >= 8) {
            out.add(in.readLong());
        }
    }
}