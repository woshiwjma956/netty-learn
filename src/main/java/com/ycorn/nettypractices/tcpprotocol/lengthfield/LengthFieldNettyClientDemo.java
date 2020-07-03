package com.ycorn.nettypractices.tcpprotocol.lengthfield;

import com.ycorn.nettypractices.tcpprotocol.TCPNettyClientHandler;
import com.ycorn.nettypractices.tcpprotocol.delimiter.MyDelimiterNettyEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/24 17:52
 */

public class LengthFieldNettyClientDemo {

    public static void main(String[] args) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class) // 这是客户端通道类型为NioSocketChannel
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        /**
                         * 具体场景参考 https://blog.csdn.net/u010853261/article/details/55803933
                         * int maxFrameLength, 每个包所能传递的最大数据包大小
                         * int lengthFieldOffset, 长度字段在字节码中的偏移量
                         * int lengthFieldLength, 长度字段所占用的字节长度
                         * int lengthAdjustment, 不仅包含有消息头和消息体的数据进行消息头的长度的调整，这样就可以只得到消息体的数据，这里的lengthAdjustment指定的就是消息头的长度
                         * int initialBytesToStrip 长度字段在消息头中间的情况，可以通过initialBytesToStrip忽略掉消息头以及长度字段占用的字节
                         */
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(1024,0,2,0,2));
                        // LengthFieldPrepender是一个编码器，主要是在响应字节数据前面添加字节长度字段 这里的长度2需要跟LengthFieldBaseFrameDecoder中占用字节长度一致 由于长度字段在起始位置并且长度为2，所以将initialBytesToStrip设置为2
                        pipeline.addLast(new LengthFieldPrepender(2));
                        // 这里需要添加StringEncoder和StringDecoder后面的handler写String才能正常接到
                        pipeline.addLast(new StringEncoder());
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
