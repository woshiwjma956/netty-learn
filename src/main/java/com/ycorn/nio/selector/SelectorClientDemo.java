package com.ycorn.nio.selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-02 14:31
 */
public class SelectorClientDemo {

    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        boolean connect = socketChannel.connect(new InetSocketAddress("localhost", 6666));

        if(!connect){
            // 连接失败不会阻塞继续执行
            while (!socketChannel.finishConnect()){
                System.out.println(" connection failed but not block");
            }
        }

        ByteBuffer buffer = ByteBuffer.wrap("hello netty".getBytes());
        socketChannel.write(buffer);

        System.in.read();


    }

}