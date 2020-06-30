package com.ycorn.nettypractices.diffwrite;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/29 11:20
 */

public class DiffBetweenCtxAndChannelWriteDemoOutBoundHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("influx outbound handler");
        ctx.write(msg);
    }
}
