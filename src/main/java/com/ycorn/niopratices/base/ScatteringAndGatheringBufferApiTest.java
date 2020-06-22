package com.ycorn.niopratices.base;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/19 11:05
 */

public class ScatteringAndGatheringBufferApiTest {

    public static void main(String[] args) throws Exception {
        ByteBuffer[] buffers = new ByteBuffer[2];
        ByteBuffer buffer1 = ByteBuffer.allocate(3);
        ByteBuffer buffer2 = ByteBuffer.allocate(5);
        buffers[0] = buffer1;
        buffers[1] = buffer2;

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(7777);
        // 绑定端口
        serverSocketChannel.socket().bind(inetSocketAddress);
        // 等待连接
        SocketChannel socketChannel = serverSocketChannel.accept();
        int messageLimit = buffer1.limit() + buffer2.limit();
        while (true) {
            long byteRead = 0;
            while (byteRead < messageLimit) {
                long read = socketChannel.read(buffers);
                byteRead += read;
                System.out.println("byteRead = " + byteRead);
                Arrays.stream(buffers).map(t -> "position:=>" + t.position() + " limit=>" + t.limit()).forEach(System.out::println);
            }

            Arrays.asList(buffers).forEach(ByteBuffer::flip);

            long bytesWrite = 0;

            while (bytesWrite < messageLimit) {
                long write = socketChannel.write(buffers);
                bytesWrite += write;
            }

            Arrays.stream(buffers).forEach(ByteBuffer::clear);
            System.out.println("byteRead = " + byteRead + ",byteWrite = " + bytesWrite + ",messageLimit = " + messageLimit);
        }

    }
}
