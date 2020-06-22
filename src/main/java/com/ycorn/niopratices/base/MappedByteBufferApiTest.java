package com.ycorn.niopratices.base;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/19 10:24
 */

public class MappedByteBufferApiTest {
    public static void main(String[] args) throws Exception {
        /**
         * 参数说明:
         * 1. 文件路径
         * 2. 读写模式 one of {@code "r"}只读, {@code "rw"}读写, {@code "rws"}读写操作同步刷新到磁盘,刷新内容和元数据, {@code "rwd"}读写同步磁盘只刷新内容
         */
        RandomAccessFile file = new RandomAccessFile("1.txt", "rw");
        /**
         * 参数说明；
         * 1.FileChannel.MapMode.READ_WRITE 使用读写模式
         * 2.直接修改的起始位置
         * 3.从起始位置映射到内存的大小（不是索引），超过字节大小将不能修改
         */
        MappedByteBuffer mappedByteBuffer = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, file.length());
        mappedByteBuffer.put(0, (byte) 'x');
        byte[] bytes = new byte[(int) file.length()];
        mappedByteBuffer.get(bytes);
        System.out.println(new String(bytes));
    }
}
