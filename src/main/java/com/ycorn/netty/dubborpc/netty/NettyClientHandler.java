package com.ycorn.netty.dubborpc.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.Callable;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-11 18:21
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter implements Callable {

    private String params;

    private ChannelHandlerContext context;

    private String result;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        context = ctx;
    }

    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        result = msg.toString();
        this.notify();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    public synchronized Object call() throws Exception {
        context.writeAndFlush((this.params));
        this.wait();
        return result;
    }

    public void setParams(String params) {
        this.params = params;
    }
}