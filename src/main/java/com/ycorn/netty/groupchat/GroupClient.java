package com.ycorn.netty.groupchat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-05 16:58
 */
public class GroupClient {

    private final String host;

    private final int port;

    public GroupClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception {
        NioEventLoopGroup worker = new NioEventLoopGroup(2);

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decode", new StringDecoder());
                            pipeline.addLast("encode", new StringEncoder());
                            pipeline.addLast("myGroupChatClientHandler", new GroupChatClientHandler());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect(this.host, this.port).sync();
            channelFuture.channel().closeFuture();
            Scanner scanner = new Scanner(System.in);

            while (scanner.hasNextLine()) {
                channelFuture.channel().writeAndFlush(scanner.nextLine());
            }

        } finally {
            worker.shutdownGracefully();
        }


    }

    public static void main(String[] args) throws Exception {
        new GroupClient("localhost", 7000).run();
    }
}