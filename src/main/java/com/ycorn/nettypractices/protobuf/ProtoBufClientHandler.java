package com.ycorn.nettypractices.protobuf;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/7/2 16:20
 */

public class ProtoBufClientHandler extends SimpleChannelInboundHandler<StudentPojo.Student> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        StudentPojo.Student stu = StudentPojo.Student.newBuilder().setAge(10).setId(1).setName("student from client").build();
        ctx.channel().writeAndFlush(stu);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StudentPojo.Student msg) throws Exception {
        System.out.println("收到服务器的对象 " + msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
