package com.ycorn.nio.niobase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-01 20:31
 */
public class FileCopyUseByChannelDemo {

    public static void main(String[] args) throws Exception {

        File file = new File("1.txt");
        FileInputStream inputStream = new FileInputStream(file);
        FileOutputStream outputStream = new FileOutputStream("3.txt");

        FileChannel inputStreamChannel = inputStream.getChannel();
        FileChannel outputStreamChannel = outputStream.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(2);

        while (true) {
            buffer.clear();
            int read = inputStreamChannel.read(buffer);
            if (read == -1) break;
            buffer.flip();
            outputStreamChannel.write(buffer);
        }
        inputStream.close();
        outputStream.close();

    }

}
