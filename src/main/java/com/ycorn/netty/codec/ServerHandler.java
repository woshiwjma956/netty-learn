package com.ycorn.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-04 16:18
 */
public class ServerHandler extends SimpleChannelInboundHandler<StudentProto.Student> {

    /**
     * 通道有数据可读
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, StudentProto.Student msg) throws Exception {
        System.out.println(String.format("Student id = %d name = %s", msg.getId(), msg.getName()));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //writeAndFlush 是 write + flush
        //将数据写入到缓存，并刷新
        //一般讲，我们对这个发送的数据进行编码
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello, client! ", CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}