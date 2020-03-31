package com.ycorn.nio.niobase;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-01 20:15
 */
public class FileWriteChannelDemo {

    public static void main(String[] args) throws Exception {
        String str = "hello world netty";
        FileOutputStream fileOutputStream = new FileOutputStream("1.txt");
        // 获取channel
        FileChannel channel = fileOutputStream.getChannel();
        // 创建byteBuffer 将字符串数据写入bytesBuffer
        ByteBuffer buffer = ByteBuffer.allocate(str.getBytes().length);
        buffer.put(str.getBytes());
        // 把byteBuffer中的数据读到channel中 因为channel 已经关联了一个outputStream 直接写入文件了
        buffer.flip(); // 一定要调用flip() 将buffer的至指针重新指向到0索引位置才能正常写入
        channel.write(buffer);
        // 关闭
        fileOutputStream.close();
    }

}