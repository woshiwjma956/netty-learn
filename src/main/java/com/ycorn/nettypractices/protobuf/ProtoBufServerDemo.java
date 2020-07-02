package com.ycorn.nettypractices.protobuf;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/7/2 16:09
 */

public class ProtoBufServerDemo {

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
                        //解码器，通过Google Protocol Buffers序列化框架动态的切割接收到的ByteBuf
                        pipeline.addLast(new ProtobufVarint32FrameDecoder());
                        //服务器端接收的是客户端StudentPojo对象, 需要进行解码
                        pipeline.addLast(new ProtobufDecoder(StudentPojo.Student.getDefaultInstance()));
                        //Google Protocol Buffers编码器
                        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                        //Google Protocol Buffers编码器
                        pipeline.addLast(new ProtobufEncoder());
                        pipeline.addLast(new ProtoBufServerHandler());
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
