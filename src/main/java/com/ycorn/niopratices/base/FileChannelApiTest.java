package com.ycorn.niopratices.base;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/12 15:50
 */

public class FileChannelApiTest {

    public static void main(String[] args) throws IOException {
//        readFromChannel();
//
//        writeToChannel();

//        transferFromApi();

        copyByNIO("2.txt", "2_copy.txt");
    }

    /**
     * 通过NIO实现文件复制
     * @param src
     * @param dest
     * @throws IOException
     */
    private static void copyByNIO(String src, String dest) throws IOException {
        File file = new File(src);
        FileInputStream inputStream = new FileInputStream(file);
        FileChannel inputChannel = inputStream.getChannel();

        FileOutputStream outputStream = new FileOutputStream(new File(dest));
        FileChannel outputChannel = outputStream.getChannel();
        // 创建buffer 数据中间保存的容器
        ByteBuffer buffer = ByteBuffer.allocate(2);

        while (true) {
            // 清空buffer
            buffer.clear();
            int read = inputChannel.read(buffer);
            // 如果已经读完了
            if (read < 0) {
                break;
            }
            // 翻转
            buffer.flip();
            // 写入
            outputChannel.write(buffer);
        }

        inputStream.close();
        outputStream.close();
    }

    /**
     * 文件拷贝 transferFrom
     *
     * @throws IOException
     */
    private static void transferFromApi() throws IOException {
        File src = new File("1.txt");
        FileInputStream inputStream = new FileInputStream(src);
        File dest = new File("1_copy.txt");
        FileOutputStream outputStream = new FileOutputStream(dest);

        FileChannel inputChannel = inputStream.getChannel();
        FileChannel outputChannel = outputStream.getChannel();
        // channel实现文件拷贝
        outputChannel.transferFrom(inputChannel, 0, src.length());

        inputStream.close();
        outputStream.close();
    }

    /**
     * 数据写入
     *
     * @throws IOException
     */
    private static void writeToChannel() throws IOException {
        // 模拟内从
        String content = "hello world netty is good!";
        // 创建一个buffer 用于保存数据
        ByteBuffer byteBuffer = ByteBuffer.allocate(content.length());
        // 将数据写入到buffer中
        byteBuffer.put(content.getBytes());
        // 注意一定要flip!!!!读写切换 想指针重新指向开头 不然写入不到任何数据
        byteBuffer.flip();
        File file = new File("output.txt");
        // 创建文件输出流
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        // 获取channel
        FileChannel channel = fileOutputStream.getChannel();
        // 将buffer中的数据写入到channel中
        channel.write(byteBuffer);
        // 关闭流自动关闭channel
        fileOutputStream.close();
    }

    /**
     * 数据读取
     *
     * @throws IOException
     */
    private static void readFromChannel() throws IOException {
        File file = new File("1.txt");
        FileInputStream fileInputStream = new FileInputStream(file);
        // 通过inputStream.getChannel() 获取当前文件的FileChannel
        FileChannel channel = fileInputStream.getChannel();
        // 创建一个buffer 用于 读数据
        ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
        // 把Channel中的数据读到buffer中
        channel.read(buffer);
        // 打印
        System.out.println(new String(buffer.array()));
        // 关闭流会自动关闭channel
        fileInputStream.close();
    }
}
