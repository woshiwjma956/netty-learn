package com.ycorn.nettypractices.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/28 17:26
 */

public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            System.out.println("请求URL: " + request.uri());
            System.out.println("请求Method: " + request.method().name());
            if (request.uri().equals("/favicon.ico")) {
                System.out.println("请求图片没有不响应");
            } else {
                ByteBuf content = ctx.alloc().buffer();
                content.writeBytes("send from server".getBytes());
                DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
                resp.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain");
                resp.headers().set(HttpHeaderNames.CONTENT_LENGTH,content.readableBytes());
                ctx.writeAndFlush(resp);
            }

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
