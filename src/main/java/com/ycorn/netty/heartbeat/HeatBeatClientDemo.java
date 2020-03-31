package com.ycorn.netty.heartbeat;

import com.ycorn.netty.groupchat.GroupChatClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Scanner;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-05 21:04
 */
public class HeatBeatClientDemo {

    public static void main(String[] args) throws Exception {
        NioEventLoopGroup work = new NioEventLoopGroup(2);

        try {
            Bootstrap bootstrap = new Bootstrap();
            ChannelFuture channelFuture = bootstrap.group(work)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //ch.pipeline().addLast(new GroupChatClientHandler());
                        }
                    })
                    .connect("localhost", 7000);
            channelFuture.sync();
            channelFuture.channel().closeFuture().sync();
            //Scanner scanner = new Scanner(System.in);
            //while (scanner.hasNextLine()){
            //    channelFuture.channel().writeAndFlush(scanner.nextLine());
            //
            //}
        } finally {
            work.shutdownGracefully();
        }

    }

}