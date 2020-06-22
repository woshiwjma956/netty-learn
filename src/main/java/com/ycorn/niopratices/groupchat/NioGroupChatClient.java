package com.ycorn.niopratices.groupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * @author : Jim Wu
 * @version 1.0
 * @function :
 * @since : 2020/6/19 16:55
 */

public class NioGroupChatClient {
    private SocketChannel socketChannel;

    private Selector selector;

    private final String host = "localhost";

    private final int port = 1234;

    /**
     * 初始化SocketClient端
     * 1. 创建selector
     * 2. 建立socket连接
     * 3. 注册到selector上
     * @throws IOException
     */
    public NioGroupChatClient() throws IOException {
        selector = Selector.open();
        socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    public void sendMsg(String msg) {
        try {
            System.out.println("I say: " + msg);
            socketChannel.write(ByteBuffer.wrap(msg.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveMsg() throws IOException {
        if (selector.select() > 0) {
            // 获取所有发生读事件的selector
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey selectionKey = it.next();
                if (selectionKey.isReadable()) {
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int read = channel.read(buffer);
                    System.out.println(channel.getLocalAddress() + " say: " + new String(buffer.array()));
                }
                it.remove();
            }
        }
    }


    public static void main(String[] args) throws IOException {
        NioGroupChatClient chatClient = new NioGroupChatClient();
        // 创建一个单独的线程进行接收消息操作
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        chatClient.receiveMsg();
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        // 启动
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            chatClient.sendMsg(s);
        }

    }
}
