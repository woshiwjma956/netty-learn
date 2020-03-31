package com.ycorn.netty.inoutboundhandler.exmaple1;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-09 11:37
 */
public class EncoderClientInit extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new MyLongEncoderHandler());
        pipeline.addLast(new SimpleChannelInboundHandler<Long>() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                ctx.writeAndFlush(123456L);
            }

            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Long msg) throws Exception {
                System.out.println("receive from server " + msg);
            }
        });
    }
}