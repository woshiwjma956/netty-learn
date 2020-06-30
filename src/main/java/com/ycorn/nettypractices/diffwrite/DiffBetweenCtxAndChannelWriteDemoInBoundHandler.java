package com.ycorn.nettypractices.diffwrite;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/29 11:19
 */

public class DiffBetweenCtxAndChannelWriteDemoInBoundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            System.out.println(request);
            ByteBuf content = Unpooled.copiedBuffer("hello client".getBytes());
            DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
            resp.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain");
            resp.headers().set(HttpHeaderNames.CONTENT_LENGTH,content.readableBytes());
            ctx.channel().writeAndFlush(resp);
        }
    }
}
