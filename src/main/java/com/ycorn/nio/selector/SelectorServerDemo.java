package com.ycorn.nio.selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-02 14:18
 */
public class SelectorServerDemo {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(6666));

        Selector selector = Selector.open();
        // 设置serverSocketChannel non-block
        serverSocketChannel.configureBlocking(false);
        // 注册serverSocketChannel到selector
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            // 没有监听到socket连接事件
            if (selector.select(1000) <= 0) {
                System.out.println("no connect event...");
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator<SelectionKey> it = selectionKeys.iterator();
            while (it.hasNext()) {
                SelectionKey selectionKey = it.next();
                if (selectionKey.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }
                if (selectionKey.isReadable()) {
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                    channel.read(buffer);
                    System.out.println("read data from client => " + new String(buffer.array()) + " socket hashCode=> "+ channel.hashCode());
                }
                it.remove();
            }

        }


    }

}