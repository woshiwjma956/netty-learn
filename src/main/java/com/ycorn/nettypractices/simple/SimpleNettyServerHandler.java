package com.ycorn.nettypractices.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/24 17:43
 */

public class SimpleNettyServerHandler extends ChannelInboundHandlerAdapter {
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

    /**
     * 有数据读取的事件
     *
     * @param ctx 上下文对象 包产了pipeLine pipeLine包含了Channel Channel包含了客户端的信息
     * @param msg msg就是客户端发送的信息
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 强制转换成一个ByteBuf 这里的ByteBuf是Netty原生的 而不是NIO中的ByteBuffer
        ByteBuf buf = (ByteBuf) msg;
        Channel channel = ctx.pipeline().channel();
        System.out.println("收到客户端" + channel.remoteAddress() + "的消息==>" + buf.toString(Charset.forName("UTF-8")));
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
        ctx.writeAndFlush(Unpooled.copiedBuffer("收到了你的消息客户端".getBytes()));
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
        cause.printStackTrace();
    }
}
