package com.ycorn.netty.codec2;

import com.ycorn.netty.codec.StudentProto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-04 16:18
 */
public class ServerHandler extends SimpleChannelInboundHandler<MyData.MyInfo> {

    /**
     * 通道有数据可读
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, MyData.MyInfo msg) throws Exception {
        if (msg.getDataType() == MyData.MyInfo.DataType.StudentType) {
            System.out.println(String.format("Student id = %d name = %s", msg.getStudent().getId(), msg.getStudent().getName()));
        } else {
            System.out.println(String.format("worker age = %d name = %s", msg.getWoker().getAge(), msg.getWoker().getName()));
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //writeAndFlush 是 write + flush
        //将数据写入到缓存，并刷新
        //一般讲，我们对这个发送的数据进行编码
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello, client! ", CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}