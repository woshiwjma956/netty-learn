package com.ycorn.nettypractices.task;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/28 15:40
 */

public class NettyTaskClientDemo {

    public static void main(String[] args) {
        NioEventLoopGroup work = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(work)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new NettyTaskClientHandler());
                    }
                });
        try {
            ChannelFuture future = bootstrap.connect("localhost", 7777).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            work.shutdownGracefully();
        }
    }
}
