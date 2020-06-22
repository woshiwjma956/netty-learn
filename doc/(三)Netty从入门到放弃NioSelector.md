### Selector 介绍

Selector 选择器也称多路复用器，NIO三大组件之一，主要用于单线程管理多个Channel，监听channel中的事件。读写操作非阻塞，提高IO运行效率。只有在通道中出现读写事件发生才会进行读写，减少系统开销，并不会为每一个连接创建一个线程，不用维护多个线程减少线程上下文切换开销。

一个 Selector对应一个Thread 可以理解为一个Thread管理了一个Selector，而Selector下面监听了多个channel的读写事件，当发生读写事件的时候再进行读写相关的处理。

### 核心类与方法

```java
Selector类核心方法
public static Selector open(); //获取一个selector
public int select(long timeout);// 获取注册到的通道 参数用来设置超时时间 返回值大于0时说明Selector 中有发生了IO操作的channel 该方法还有一个不带参数的阻塞方法，带超时的本质也是阻塞方法只是阻塞时间为超时时长
public Set<SelectorKey> selectedKeys();// 返回所有发生了IO操作的SelectionKey 而SelectorKey中关联了具体的channel
public int selectNow();// 立刻返回selector中的SelectionKey的数量
public Selector wakeup();// 唤醒selector
```

```java
SelectionKey 表示了Selector和channel的注册关系
核心常量
public static final int OP_READ = 1 << 0; // 读事件
public static final int OP_WRITE = 1 << 2; // 写事件
public static final int OP_CONNECT = 1 << 3; // 连接建立事件
public static final int OP_ACCEPT = 1 << 4;// 可以连接事件
核心方法
public Selector selector();// 通过SelectionKey获取Selector
public SelectableChannel channel();// 获取SelectionK绑定的Channel
public SelectionKey interestOps(int ops);// 改变监听事件
public boolean isAccetable(); // 是否可接受连接
public boolean isReadable(); // 是否可读
public boolean isWritable(); //是否可写
```

```java
//ServerSocketChannel Nio中处理ServerSocket服务端核心类 类似传统ServerScoket
// 核心方法
public static ServerSocketChannel open();// 静态方法返回一个ServerSocketChannel
public ServerSocketChannel bind(SocketAddress adress);// 设置服务绑定的端口
public SelectableChannel configureBlocking(boolean block);// 设置为阻塞或者非阻塞
public SelectionKey register(Selector sel,int ops);// 注册到一个Selector中 传入关注的事件类型
```

```java
//SocketChannel Nio中处理Socket客户端的核心类 类似传统的Socket
// 核心方法
public static SocketChannel open();// 静态方法返回一个SocketChannel
public SelectableChannel configureBlocking(boolean block);// 设置为阻塞或者非阻塞
public boolean connect(SocketAddress address);// 连接一个Socket服务器
public boolean finishConnect();// 是否连接完成
public int write(ByteBuffer buffer);// 向通道中写入数据
public int read(ByteBuffer buffer);// 将通道中的数据读到buffer中
public SelectionKey register(Selector sel,int ops,Object attament);// 注册到Selector中 最后一个参数可以附加一个共享的数据
```

### 代码演示

```java
// ServerSocket端
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
```

```java
// Socket客户端
public class SelectClientApiTest {

    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.socket().connect(new InetSocketAddress("localhost",9999));
        socketChannel.configureBlocking(false);

        if(!socketChannel.isConnected()){
            while (!socketChannel.finishConnect()){
                System.out.println("socket channel 连接上 Server SocketChannel");
            }
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap("hello Nio".getBytes());
        socketChannel.write(byteBuffer);

        System.in.read();
    }
}
```