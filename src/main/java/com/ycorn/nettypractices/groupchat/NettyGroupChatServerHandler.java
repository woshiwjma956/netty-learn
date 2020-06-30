package com.ycorn.nettypractices.groupchat;

import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.ChannelMatchers;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/29 15:24
 */

public class NettyGroupChatServerHandler extends SimpleChannelInboundHandler<String> {

    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + " is online ...!");
        channelGroup.writeAndFlush(ctx.channel().remoteAddress() + " is online ...!");
        channelGroup.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + " is offline ...!");
        channelGroup.remove(ctx.channel());
        channelGroup.writeAndFlush(ctx.channel().remoteAddress() + " is offline ...!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel self = ctx.channel();
        // 不是自己
        ChannelMatcher matcher = channel -> channel != self;
        channelGroup.writeAndFlush(self.remoteAddress() + " say: " + msg, matcher);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
