package com.ycorn.nettypractices.tcpprotocol.custom;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/24 17:55
 */

public class MyTcpMessageClientHandler extends SimpleChannelInboundHandler<MyTcpMessage> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        MyTcpMessage msg = new MyTcpMessage();
        msg.setContent("helloServer".getBytes());
        msg.setLength(msg.getContent().length);
        ctx.channel().writeAndFlush(msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MyTcpMessage msg) throws Exception {
        System.out.println("收到服务端的长度为" + msg.getLength() + "的回信: " + new String(msg.getContent()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
