package com.ycorn.nettypractices.ws;

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
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/30 10:19
 */

public class NettyWebsocketServerDemo {

    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        // 基于Http协议 添加Http编解码器
                        pipeline.addLast(new HttpServerCodec());
                        // 已块方式写 添加chunkedWriteHandler处理器 大数据流的处理
                        pipeline.addLast(new ChunkedWriteHandler());
                        // http数据在传输过程中是分段传输,HttpObjectAggregator可以将多段复合，聚合成FullHttpRequest或FullHttpResponse
                        pipeline.addLast(new HttpObjectAggregator(1024 * 8));
                        // 添加webSocketServerProtocolHandler 数据以帧Frame形式传输
                        // 指定浏览器请求的路径为ws://localhost:port/hello
                        // WebSocket都是以帧传输不同数据类型对应的frame也不同
                        // WebSocketServerProtocolHandler主要作用是将http 升级与ws 通过状态码101保持长连接
                        pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));
                        pipeline.addLast(new MyNettyWebSocketServerHandler());

                    }
                });
        try {
            ChannelFuture future = serverBootstrap.bind(4444).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
