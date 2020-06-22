package com.ycorn.niopratices.base;


import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/19 11:49
 */

public class SelectorServerApiTest {

    public static void main(String[] args) throws Exception {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9999));
        // 设置非阻塞
        serverSocketChannel.configureBlocking(false);
        // 创建一个selector
        Selector selector = Selector.open();
        // 调用channel register 方法将 channel注册到selector 中
        /**
         * 参数一:selector 主要注册到的selector
         * 参数二:注册到selector 关心的模式 SelectionKey中的常量
         * 返回当前注册到select中的唯一Key
         */
        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            int select = selector.select(1000);
            // 如果select方法返回0 说明 selector 中没有注册任何通道
            if (select == 0) {
                System.out.println("服务器等待1秒,没有客户端连接");
                continue;
            }
            // 如果select 方法返回不为0 说明可以获取到注册到select中通道关心的事件
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectionKeys.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                // 如果发生是OP_ACCEPT事件,表明有新客户端连接
                if (key.isAcceptable()) {
                    // 获取到客户端的channel
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    // 设置非阻塞
                    socketChannel.configureBlocking(false);
                    // 注册到selector 中 关注READ事件 并关联一个buffer
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }
                if (key.isReadable()) {
                    // 如果发生的读事件 调用SelectorKey的.channel方法获取channel 并强制转换成SocketChannel
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    // 拿到之前注册到selector 上的 附加buffer
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    // 将数据读到buffer中
                    socketChannel.read(buffer);
                    System.out.println(new String(buffer.array()));
                }
                // 移除迭代器中的key 防止重复操作
                it.remove();
            }
        }
    }
}
