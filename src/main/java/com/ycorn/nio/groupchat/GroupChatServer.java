package com.ycorn.nio.groupchat;

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
 * @create 2020-03-02 21:44
 */
public class GroupChatServer {

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    private static final int PORT = 6667;

    public GroupChatServer() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listen() {
        try {
            while (true) {
                if (selector.select() > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        if (selectionKey.isAcceptable()) {
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            System.out.println(socketChannel.getRemoteAddress() + " online...");
                        }
                        if (selectionKey.isReadable()) {
                            // 读取客户端发送的数据
                            readInfo(selectionKey);
                        }
                        iterator.remove();
                    }

                } else {
                    System.out.println(" wait for connection ..");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //try {
            //    //serverSocketChannel.close();
            //} catch (IOException e) {
            //    e.printStackTrace();
            //}
        }
    }

    private void readInfo(SelectionKey selectionKey) {
        SocketChannel channel = null;
        try {
            channel = (SocketChannel) selectionKey.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int read = channel.read(buffer);
            if (read > 0) {
                String msg = new String(buffer.array());
                System.out.println("receive msg from " + channel.getRemoteAddress() + " msg => " + msg);
                // redirect msg to other client
                sendMsgToOtherClient(msg, channel);
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                System.out.println(((SocketChannel) selectionKey.channel()).getRemoteAddress() + " offLine ...");
                selectionKey.cancel();
                channel.close();

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void sendMsgToOtherClient(String msg, SocketChannel self) {
        try {
            Set<SelectionKey> selectionKeys = selector.keys();
            for (SelectionKey selectionKey : selectionKeys) {
                Channel channel = selectionKey.channel();
                if (channel instanceof SocketChannel && !channel.equals(self)) {
                    SocketChannel socketChannel = (SocketChannel) channel;
                    socketChannel.write(ByteBuffer.wrap(msg.getBytes()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public static void main(String[] args) {
        GroupChatServer groupChatServer = new GroupChatServer();

        groupChatServer.listen();
    }
}