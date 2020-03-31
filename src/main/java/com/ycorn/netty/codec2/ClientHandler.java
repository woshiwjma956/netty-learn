package com.ycorn.netty.codec2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-04 16:33
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {
    //当通道就绪就会触发该方法
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client " + ctx);
        int random = ThreadLocalRandom.current().nextInt();
        MyData.MyInfo info = null;
        if (random % 2 == 0) {
            info = MyData.MyInfo.newBuilder().setDataType(MyData.MyInfo.DataType.StudentType).setStudent(MyData.Student.newBuilder().setName("student").setId(1).build()).build();
        } else {
            info = MyData.MyInfo.newBuilder().setDataType(MyData.MyInfo.DataType.WorkerType).setWoker(MyData.Worker.newBuilder().setName("worker").setAge(12).build()).build();
        }
        ctx.writeAndFlush(info);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;

        System.out.println(" 服务器 发送的数据" + buf.toString(CharsetUtil.UTF_8));
        System.out.println("服务器的地址： " + ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}