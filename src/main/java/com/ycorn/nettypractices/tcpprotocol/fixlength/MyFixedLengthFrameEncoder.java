package com.ycorn.nettypractices.tcpprotocol.fixlength;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/7/3 10:52
 */

public class MyFixedLengthFrameEncoder extends MessageToByteEncoder<String> {

    private int length;

    public MyFixedLengthFrameEncoder(int length) {
        this.length = length;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        // 如果长度大于 拆分
        while (msg.length() > length) {
            String sendMsg = msg.substring(0, length);
            ctx.channel().writeAndFlush(sendMsg);
            msg = msg.substring(length);
        }
        // 如果长度不足补全
        if (msg.length() < length) {
            msg = appendToLength(msg);
        }
        out.writeBytes(msg.getBytes());
    }

    private String appendToLength(String msg) {
        StringBuilder builder = new StringBuilder(msg);
        for (int i = 0; i < length - msg.length(); i++) {
            builder.append(" ");
        }

        return builder.toString();
    }

}
