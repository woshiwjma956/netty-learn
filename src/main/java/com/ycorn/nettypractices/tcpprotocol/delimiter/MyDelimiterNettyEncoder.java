package com.ycorn.nettypractices.tcpprotocol.delimiter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/7/3 15:23
 */

public class MyDelimiterNettyEncoder extends MessageToByteEncoder<String> {
    private String delimiter;

    public MyDelimiterNettyEncoder(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        // 在响应的数据后面添加分隔符
        ctx.channel().writeAndFlush(Unpooled.wrappedBuffer((msg + delimiter).getBytes()));
    }
}
