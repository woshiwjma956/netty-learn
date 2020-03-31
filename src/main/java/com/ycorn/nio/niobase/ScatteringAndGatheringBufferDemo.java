package com.ycorn.nio.niobase;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-01 23:40
 */
public class ScatteringAndGatheringBufferDemo {
    /**
     * Scatting：将数据写入到buffer时，可以使用buffer数组，依次写入
     * Gathering：从buffer读取数据时，可以采用buffer数组，依次读取
     */
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(7777);
        // 绑定端口
        serverSocketChannel.socket().bind(inetSocketAddress);

        ByteBuffer[] byteBuffers = new ByteBuffer[2];

        byteBuffers[0] = ByteBuffer.allocate(5);
        byteBuffers[1] = ByteBuffer.allocate(3);

        int messageLength = 8;

        // 等待连接
        SocketChannel socketChannel = serverSocketChannel.accept();

        while (true) {
            long byteRead = 0;
            while (byteRead < messageLength) {
                long read = socketChannel.read(byteBuffers);
                byteRead += read;
                System.out.println("byteRead = " + byteRead);
                Arrays.stream(byteBuffers).map(t -> "position:=>" + t.position() + " limit=>" + t.limit()).forEach(System.out::println);
            }

            Arrays.asList(byteBuffers).forEach(ByteBuffer::flip);

            long bytesWrite = 0;

            while (bytesWrite < messageLength) {
                long write = socketChannel.write(byteBuffers);
                bytesWrite += write;
            }

            Arrays.stream(byteBuffers).forEach(ByteBuffer::clear);
            System.out.println("byteRead = " + byteRead + ",byteWrite = " + bytesWrite + ",messageLength = " + messageLength);
        }
    }

}