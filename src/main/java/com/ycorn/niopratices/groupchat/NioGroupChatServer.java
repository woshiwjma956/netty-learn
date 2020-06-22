package com.ycorn.niopratices.groupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
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
 * @since : 2020/6/19 16:34
 */

public class NioGroupChatServer {

    public ServerSocketChannel serverSocketChannel;

    public final int port = 1234;

    public Selector selector;

    /**
     * 构造器中初始化ServerSocketChannel Selector 并将ServerSocketChannel注册到Selector中
     *
     * @throws IOException
     */
    public NioGroupChatServer() throws IOException {
        // 创建serverSocketChannel
        serverSocketChannel = ServerSocketChannel.open();
        // 绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        // 设置非阻塞
        serverSocketChannel.configureBlocking(false);
        // 创建selector 并将channel注册到selector中
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * 监听方法
     */
    public void listen() {
        try {
            while (true) {
                // 每个1s获取一下Selector中所有触发事件的channel
                if (selector.select() > 0) {
                    // 获取所有关注的selector中触发的事件
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> it = selectionKeys.iterator();
                    while (it.hasNext()) {
                        SelectionKey sk = it.next();
                        // 如果是接受说明是socket Client连接 将这个client 注册到Selector中 用于转发
                        if (sk.isAcceptable()) {
                            SocketChannel sc = serverSocketChannel.accept();
                            // 设置非阻塞
                            sc.configureBlocking(false);
                            // 注册selector中关注读事件
                            sc.register(selector, SelectionKey.OP_READ);
                            System.out.println(sc.getRemoteAddress() + " is online...");
                        }
                        if (sk.isReadable()) {
                            // 如果是读事件读取信息
                            readInfo(sk);
                        }
                        it.remove();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取客户端发送的消息
     *
     * @param selectionKey
     */
    private void readInfo(SelectionKey selectionKey) {
        SocketChannel sc = (SocketChannel) selectionKey.channel();
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int read = sc.read(buffer);
            if (read > 0) {
                String msg = new String(buffer.array());
                System.out.println(sc.getRemoteAddress() + "说: " + msg);
                // 转发其他客户端
                sendToOther(msg, sc);
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                // 异常关闭selector 和channel
                System.out.println(sc.getRemoteAddress() + " is offline..");
                sc.close();
                selectionKey.cancel();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 消息转发
     *
     * @param msg
     * @param self
     * @throws IOException
     */
    private void sendToOther(String msg, SocketChannel self) throws IOException {
        // 这里使用keys() API获取所有注册到selector中的Key而不是selectedKeys()获取事件发生key
        Set<SelectionKey> selectionKeys = selector.keys();
        // 这里不能使用it 因为之前去读信息的时候已经使用过了迭代器的next 指针已经下移
        selectionKeys.forEach(sk -> {
            // 只要不是自己转发出去
            if (sk.channel() instanceof SocketChannel && !sk.channel().equals(self)) {
                SocketChannel socketChannel = (SocketChannel) sk.channel();
                try {
                    socketChannel.write(ByteBuffer.wrap(msg.getBytes()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public static void main(String[] args) throws IOException {
        NioGroupChatServer nioGroupChatServer = new NioGroupChatServer();
        nioGroupChatServer.listen();
    }
}
