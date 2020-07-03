package com.ycorn.nettypractices.tcpprotocol.custom;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/24 17:43
 */

public class MyTcpMessageServerHandler extends SimpleChannelInboundHandler<MyTcpMessage> {
    /**
     * 当连接建立成功之后的操作
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端" + ctx.pipeline().channel().remoteAddress() + "建立连接成功");
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MyTcpMessage msg) throws Exception {
        System.out.println("收到客户端" + ctx.channel().remoteAddress() + "的消息长度为" + msg.getLength() + "==>" + new String(msg.getContent()));
    }

    /**
     * 数据读取完毕事件
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 服务端给客户端写数据 使用Netty的Unpooled 非池化类构建一个ByteBuf
        MyTcpMessage msg = new MyTcpMessage();
        msg.setContent("收到了你的消息客户端".getBytes());
        msg.setLength(msg.getContent().length);
        ctx.channel().writeAndFlush(msg);
    }

    /**
     * 出现异常事件
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 出现异常关闭连接 打印错误栈
        ctx.close();
    }
}
