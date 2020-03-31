package com.ycorn.netty.dubborpc.netty;

import com.ycorn.netty.dubborpc.provider.HelloServiceImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-11 18:16
 */
public class DubboServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 自定义协议头 dubbo#HelloService#hello#args
        System.out.println("receive from client: " + msg);
        if (msg.toString().startsWith("dubbo")) {
            String result = new HelloServiceImpl().hello(msg.toString().split("#")[3]);
            ctx.writeAndFlush(result);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}