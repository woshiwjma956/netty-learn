package com.ycorn.netty.tcpprotocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-09 16:34
 */
public class TCPProtocolClientInit extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new TCPProtocolEncoder());
        pipeline.addLast(new TCPProtocolDecoder());
        pipeline.addLast(new SimpleChannelInboundHandler<MessageProtocol>() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                for (int i = 0; i < 5; i++) {
                    MessageProtocol messageProtocol = new MessageProtocol();
                    byte[] content = ("eat hot food" + i).getBytes();
                    messageProtocol.setContent(content);
                    messageProtocol.setLen(content.length);
                    ctx.writeAndFlush(messageProtocol);
                }
            }

            @Override
            protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) throws Exception {
                System.out.println("receive from server:" + msg);
                System.out.println();
                System.out.println();
                System.out.println();

            }
        });
    }
}