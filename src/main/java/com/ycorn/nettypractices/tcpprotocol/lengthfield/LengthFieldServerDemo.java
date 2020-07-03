package com.ycorn.nettypractices.tcpprotocol.lengthfield;

import com.ycorn.nettypractices.tcpprotocol.TCPNettyServerHandler;
import com.ycorn.nettypractices.tcpprotocol.delimiter.MyDelimiterNettyEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/7/3 10:50
 */

public class LengthFieldServerDemo {

    public static void main(String[] args) {
        // 创建boosGroup
        NioEventLoopGroup boosGroup = new NioEventLoopGroup(1);
        // 创建workerGroup 参数表示具体线程数，一个EventLoopGroup中有多少个EventLoop 默认不传参数CPU核数*2
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        // 服务端配置对象
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boosGroup, workerGroup) // 传入 bossGroup 和worker Group
                .channel(NioServerSocketChannel.class) //使用NioSocketChannel的类型 这里使用NioServerSocketChannel作为通道的实现
                .option(ChannelOption.SO_BACKLOG, 128) // 设置boosGroup线程队列得到连接个数
                .childOption(ChannelOption.SO_KEEPALIVE, true)//设置workerGroup 保持活动连接状态
                .childHandler(new ChannelInitializer<SocketChannel>() {
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
                        pipeline.addLast(new TCPNettyServerHandler());
                    }
                });
        // 启动服务绑定端口
        ChannelFuture future = null;
        try {
            future = serverBootstrap.bind(6666).sync();
            // 关闭通道监听
            future.channel().closeFuture().sync();
            System.out.println(123123);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 优雅关闭boosGroup 和 workerGroup
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
