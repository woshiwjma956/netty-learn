package com.ycorn.nettypractices.protobuf;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/7/2 16:18
 */

public class ProtoBufClientDemo {

    public static void main(String[] args) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(worker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
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
                        // 自定义handler
                        pipeline.addLast(new ProtoBufClientHandler());
                    }
                });

        try {
            ChannelFuture future = bootstrap.connect("localhost", 4444).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            worker.shutdownGracefully();
        }
    }
}
