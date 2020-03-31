package com.ycorn.nio.niobase;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-01 23:29
 */
public class MappedByteBufferDemo {
    /**
     * 直接在内存中修改，不用操作系统再拷贝一次
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        RandomAccessFile randomAccessFile = new RandomAccessFile("1.txt", "rw");

        FileChannel channel = randomAccessFile.getChannel();


        /**
         * 参数说明；
         * 1.FileChannel.MapMode.READ_WRITE 使用读写模式
         * 2.直接修改的起始位置
         * 3.从起始位置映射到内存的大小（不是索引），超过字节大小将不能修改
         */

        MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 5);
        mappedByteBuffer.put(0, (byte) 'H');
        mappedByteBuffer.put(3, (byte) '6');
        randomAccessFile.close();
    }

}