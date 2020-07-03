package com.ycorn.nettypractices.tcpprotocol.delimiter;

import com.ycorn.nettypractices.tcpprotocol.TCPNettyClientHandler;
import com.ycorn.nettypractices.tcpprotocol.fixlength.MyFixedLengthFrameEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/24 17:52
 */

public class DelimiterNettyClientDemo {

    public static void main(String[] args) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class) // 这是客户端通道类型为NioSocketChannel
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        String delimiter = "$$";
                        // 自定义分隔符编码器
                        pipeline.addLast(new MyDelimiterNettyEncoder(delimiter));
                        // 被按照$$进行分隔，1024指的是分隔的最大长度，读取到1024个字节的数据之后，若还是未读取到分隔符，则舍弃当前数据段
                        // 放在StringDecoder之前 先将分隔符去掉
                        pipeline.addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer(delimiter.getBytes())));
                        // 将分隔之后的字节数据转换为字符串 会传给后面的 SimpleChannelInboundHandler<String>
                        pipeline.addLast(new StringDecoder());
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
