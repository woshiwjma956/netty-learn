package com.ycorn.nio.niobase;

import java.nio.IntBuffer;

/**
 * 描述:
 * NioBuffer Demo
 *
 * @author JimWu
 * @create 2020-03-01 17:12
 */
public class NioBufferDemo {

    public static void main(String[] args) {
        // 使用Buffer.allocate 创建一个指定大小的buffer
        IntBuffer intBuffer = IntBuffer.allocate(5);

        for (int i = 0; i < intBuffer.capacity(); i++) {
            int i1 = i * 2;
            System.out.println(String.format("put %d in buffer", i1));
            intBuffer.put(i1);
        }

        // buffer 读写模式切换
        intBuffer.flip();

        while (intBuffer.hasRemaining()) {
            System.out.println("take buffer => "+intBuffer.get());
        }
    }

}