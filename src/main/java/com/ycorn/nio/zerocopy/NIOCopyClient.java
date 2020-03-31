package com.ycorn.nio.zerocopy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-03 11:40
 */
public class NIOCopyClient {

    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7070));

        File fileName = new File("1.txt");
        FileInputStream fileInputStream = new FileInputStream(fileName);
        FileChannel fileChannel = fileInputStream.getChannel();
        long start = System.currentTimeMillis();
        // linux 直接使用transferTo即可
        // windows 下 一次调用transferTo 只能传入8M ,需要分段传输文件
        // transferTo底层使用零拷贝
        long transfer = fileChannel.transferTo(0, fileName.length(), socketChannel);

        System.out.println("send " + transfer + " bytes use +" + (System.currentTimeMillis() - start));
        fileChannel.close();
    }

}