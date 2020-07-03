package com.ycorn.nettypractices.tcpprotocol.delimiter;

import com.ycorn.nettypractices.tcpprotocol.TCPNettyServerHandler;
import com.ycorn.nettypractices.tcpprotocol.fixlength.MyFixedLengthFrameEncoder;
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
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/7/3 10:50
 */

public class DelimiterServerDemo {

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
                        // 添加处理器
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        String delimiter = "$$";
                        // 自定义分隔符编码器
                        pipeline.addLast(new MyDelimiterNettyEncoder(delimiter));
                        // 被按照$$进行分隔，1024指的是分隔的最大长度，读取到1024个字节的数据之后，若还是未读取到分隔符，则舍弃当前数据段
                        // 放在StringDecoder之前 先将分隔符去掉
                        pipeline.addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer(delimiter.getBytes())));
                        // 将分隔之后的字节数据转换为字符串 会传给后面的 SimpleChannelInboundHandler<String>
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
