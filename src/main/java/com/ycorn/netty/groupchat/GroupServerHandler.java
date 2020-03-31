package com.ycorn.netty.groupchat;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-05 16:43
 */
public class GroupServerHandler extends SimpleChannelInboundHandler<String> {

    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 用户上线提醒
        Channel channel = ctx.channel();

        channelGroup.writeAndFlush(Unpooled.copiedBuffer("----" + channel.remoteAddress() + " is online...", CharsetUtil.UTF_8));
        channelGroup.add(channel);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + " is online...");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("GroupChat Size: " + channelGroup.size());
        channelGroup.writeAndFlush(Unpooled.copiedBuffer("----" + ctx.channel().remoteAddress() + " is offline...", CharsetUtil.UTF_8));

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        System.out.println(channel.remoteAddress() + " is offline...");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel channel = ctx.channel();

        channelGroup.forEach(c -> {
            if (channel != c) {
                c.writeAndFlush("[Client] " + channel.remoteAddress() + " say: " + msg);
            } else {
                c.writeAndFlush("[I] " + " say: " + msg);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}