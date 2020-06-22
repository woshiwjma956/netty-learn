package com.ycorn.niopratices.base;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/19 14:53
 */

public class SelectClientApiTest {

    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.socket().connect(new InetSocketAddress("localhost",9999));
        socketChannel.configureBlocking(false);

        if(!socketChannel.isConnected()){
            while (!socketChannel.finishConnect()){
                System.out.println("socket channel 连接上 Server SocketChannel");
            }
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap("hello Nio".getBytes());
        socketChannel.write(byteBuffer);

        System.in.read();
    }
}
