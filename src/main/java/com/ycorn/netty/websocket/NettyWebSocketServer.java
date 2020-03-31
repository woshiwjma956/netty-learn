package com.ycorn.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-07 19:30
 */
public class NettyWebSocketServer {

    public static void main(String[] args) throws Exception{
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(8);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加基于HTTP协议的编码和解码器
                            pipeline.addLast(new HttpServerCodec());

                            // 以块方式写,添加ChunkedwWriteHandler
                            pipeline.addLast(new ChunkedWriteHandler());

                            // Http在传输大数据时候采用分段传输 这就是为什么当浏览器在传输大量数据需要多次请求
                            pipeline.addLast(new HttpObjectAggregator(8192));
                            /**
                             * 添加 WS 处理器
                             * 1.对应WS 采用帧 frame进行传输
                             * 2.WebSocketFrame 下面6个子类
                             * 3.浏览器请求 ws://localhost:7000/xxx 表示请求的URI
                             * 4.WebSocketServerProtocolHandler 核心功能是将HTTP协议升级为WS协议
                             * 5. 通过状态码101
                             */
                            pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));
                            pipeline.addLast(new NettyWebSocketServerHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(7000).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

}