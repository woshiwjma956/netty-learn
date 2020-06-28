package com.ycorn.nettypractices.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/24 17:55
 */

public class SimpleNettyClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 使用上下文对象创建一个ByteBuf内存管理器
        ByteBuf buffer = ctx.alloc().buffer();
        // 调用ByteBuf中的writeBytes方法将byte数据写入buf
        buffer.writeBytes("helloServer!".getBytes());
        ctx.writeAndFlush(buffer);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("收到服务端的回信: " + buf.toString(Charset.defaultCharset()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }
}
