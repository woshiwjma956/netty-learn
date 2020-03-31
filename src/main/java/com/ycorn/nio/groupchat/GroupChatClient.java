package com.ycorn.nio.groupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-02 22:27
 */
public class GroupChatClient {

    private Selector selector;

    private SocketChannel channel;

    private final static String HOST = "localhost";

    private final static int PORT = 6667;

    public GroupChatClient() {
        try {
            selector = Selector.open();
            channel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
            channel.configureBlocking(false);
            channel.register(selector,SelectionKey.OP_READ);
            //channel.connect();
            System.out.println(channel.getLocalAddress() +" online...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg(String msg) {
        try {
            System.out.println("I say: " + msg);
            msg = channel.getLocalAddress() + " say: " + msg;
            msg = msg.substring(1);
            channel.write(ByteBuffer.wrap(msg.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reviceMsg() {

        try {
            if (selector.select() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                if (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isReadable()) {
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        channel.read(buffer);
                        System.out.println(new String(buffer.array()));
                    }

                }
                iterator.remove();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GroupChatClient groupChatClient = new GroupChatClient();

        new Thread(() -> {
            while (true) {
                groupChatClient.reviceMsg();
                try {
                    Thread.currentThread().sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            groupChatClient.sendMsg(s);
        }
    }
}