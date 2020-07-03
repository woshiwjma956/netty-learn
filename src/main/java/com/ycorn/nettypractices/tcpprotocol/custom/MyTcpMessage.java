package com.ycorn.nettypractices.tcpprotocol.custom;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/7/3 16:01
 */

public class MyTcpMessage {

    private int length;

    private byte[] content;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
