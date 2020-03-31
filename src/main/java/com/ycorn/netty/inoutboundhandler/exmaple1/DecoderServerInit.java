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
 * @create 2020-03-09 11:31
 */
public class DecoderServerInit extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new MyLongDecoderHandler());
        pipeline.addLast(new SimpleChannelInboundHandler<Long>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Long msg) throws Exception {
                System.out.println("receive from client + " + msg);
            }
        });
    }
}