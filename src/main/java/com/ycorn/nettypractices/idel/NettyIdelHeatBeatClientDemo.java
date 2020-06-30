package com.ycorn.nettypractices.idel;

import com.ycorn.netty.groupchat.GroupChatClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/29 17:27
 */

public class NettyIdelHeatBeatClientDemo {

    public static void main(String[] args) throws Exception {
        NioEventLoopGroup work = new NioEventLoopGroup(2);

        try {
            Bootstrap bootstrap = new Bootstrap();
            ChannelFuture channelFuture = bootstrap.group(work)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    ctx.writeAndFlush("hello Server");
                                }
                            });
                        }
                    })
                    .connect("localhost", 4444);
            channelFuture.sync();
            channelFuture.channel().closeFuture().sync();
            //}
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            work.shutdownGracefully();
        }

    }
}
