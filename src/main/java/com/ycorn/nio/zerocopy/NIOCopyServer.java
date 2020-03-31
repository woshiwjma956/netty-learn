package com.ycorn.nio.zerocopy;


import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-03 11:37
 */
public class NIOCopyServer {

    public static void main(String[] args) throws Exception {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(7070));
        ByteBuffer buffer = ByteBuffer.allocate(4096);

        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            int readCount = 0;
                int read = 0;
                while (read != -1) {
                    try {
                        read = socketChannel.read(buffer);
                        readCount += read;
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                    buffer.rewind();
                }

                System.out.println("read " + readCount + " byte");
                System.out.println(new String(buffer.array()));
            // 倒带 可以继续使用这个buffer 进行读取
        }
    }

}