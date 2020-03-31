package com.ycorn.netty.tcpprotocol;

import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-09 16:31
 */
public class MessageProtocol {
    private int len;

    private byte[] content;

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "MessageProtocol{" +
                "len=" + len +
                ", content=" + new String(this.content, CharsetUtil.UTF_8) +
                '}';
    }
}