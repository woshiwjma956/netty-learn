package com.ycorn.netty.tcpprotocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;


/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-09 16:29
 */
public class TCPProtocolServerInit extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new TCPProtocolDecoder());
        pipeline.addLast(new TCPProtocolEncoder());
        pipeline.addLast(new SimpleChannelInboundHandler<MessageProtocol>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) throws Exception {
                System.out.println("receive from client: " + msg);
                System.out.println();
                System.out.println();
                System.out.println();
                MessageProtocol messageProtocol = new MessageProtocol();
                byte[] content = msg.getContent();
                String resp = new String(content, CharsetUtil.UTF_8) + " delicious";
                messageProtocol.setContent(resp.getBytes(CharsetUtil.UTF_8));
                messageProtocol.setLen(resp.getBytes().length);
                ctx.writeAndFlush(messageProtocol);

            }
        });
    }
}