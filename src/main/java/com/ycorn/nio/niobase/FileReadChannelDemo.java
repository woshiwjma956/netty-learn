package com.ycorn.nio.niobase;


import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-01 20:25
 */
public class FileReadChannelDemo {

    public static void main(String[] args) throws Exception {
        File file = new File("1.txt");
        FileInputStream inputStream = new FileInputStream(file);
        FileChannel channel = inputStream.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate((int) file.length());

        //Reads a sequence of bytes from this channel into the given buffer
        channel.read(buffer);
        // 需要调用flip 将position重新至为0
        buffer.flip();

        System.out.println(new String(buffer.array()));

        inputStream.close();
    }

}