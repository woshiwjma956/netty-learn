package com.ycorn.nettypractices.protobuf;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/7/2 16:14
 */

public class ProtoBufServerHandler extends SimpleChannelInboundHandler<StudentPojo.Student> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StudentPojo.Student msg) throws Exception {
        System.out.println(String.format("收到客户端%s的信息: %s", ctx.channel().remoteAddress(), msg.toString()));
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        StudentPojo.Student student = StudentPojo.Student.newBuilder().setId(1).setGender(StudentPojo.Student.Gender.MALE).setAge(30).build();
        ctx.channel().writeAndFlush(student);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
