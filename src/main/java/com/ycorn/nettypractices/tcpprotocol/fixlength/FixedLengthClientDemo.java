package com.ycorn.nettypractices.tcpprotocol.fixlength;

import com.ycorn.nettypractices.tcpprotocol.TCPNettyClientHandler;
import com.ycorn.nettypractices.tcpprotocol.fixlength.MyFixedLengthFrameEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/24 17:52
 */

public class FixedLengthClientDemo {

    public static void main(String[] args) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class) // 这是客户端通道类型为NioSocketChannel
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // 自定义的定长编码器
                        pipeline.addLast(new MyFixedLengthFrameEncoder(512));
                        // StringDecoder放在 FixLengthFrameDecode之前因为先将String decode之后再进行长度decode才能保证数据的正确传输
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(new FixedLengthFrameDecoder(512));
                        pipeline.addLast(new TCPNettyClientHandler());
                    }
                });

        try {
            ChannelFuture future = bootstrap.connect("localhost", 6666).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
