package com.ycorn.niopratices.base;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.stream.IntStream;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/8 10:09
 */

public class BufferApiTest {

    public static void main(String[] args) {
        IntBuffer buffer = IntBuffer.allocate(10);
        // 放入buffer
        IntStream.range(0,buffer.capacity()).forEach(buffer::put);
        // 翻转
        buffer.flip();
        // 取出
        while (buffer.hasRemaining()){
            System.out.println(buffer.get());
        }
    }
}
