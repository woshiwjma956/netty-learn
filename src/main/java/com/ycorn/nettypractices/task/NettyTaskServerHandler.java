package com.ycorn.nettypractices.task;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.EventExecutorGroup;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/28 15:35
 */

public class NettyTaskServerHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("receive msg from client : " + buf.toString(Charset.defaultCharset()));
        // 提交一个异步任务到上下文的eventLoop的线程池中
//        ctx.channel().eventLoop().execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    TimeUnit.SECONDS.sleep(5);
//                    System.out.println("sync after 5 seconds  runnable task ... ");
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
        /**
         * 启动一个延时任务
         * 参数2 延时时间
         * 参数3 延时时间单位
         */
        ctx.channel().eventLoop().schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("启动一个延时3秒的任务...");
            }
        }, 3, TimeUnit.SECONDS);
        /**
         * 启动一个定时任务
         * 参数2: delay 延时时长
         * 参数3: period 间隔时长
         * 参数4: timeUnit 时间单位
         */
        ctx.channel().eventLoop().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("启动一个延时3秒 每1秒执行一次定时任务..");
            }
        }, 3, 1, TimeUnit.SECONDS);
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello client!".getBytes()));
        System.out.println("server go on...说明提交一个异步的任务");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}
