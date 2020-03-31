package com.ycorn.netty.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-04 21:34
 */
public class MyHttpInitializer extends ChannelInitializer<SocketChannel> {


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast("MyHttpServerCodec",new HttpServerCodec());
        ch.pipeline().addLast("MyHttpServerHandler",new MyHttpServerHandler());
        System.out.println("server has already");
    }
}