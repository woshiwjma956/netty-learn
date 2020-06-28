## Netty简介

Netty是一个基于NIO的快速易用开发网络应用程序的高性能框架，极大的简化了网络编程如TCP何UDP的服务器。Netty设计实现许多协议如FTP SMTP HTTP等。

- 设计 支持不同传输模式阻塞或者非阻塞，基于可扩展的事件模型，高可定制的线程模型，真正实现了无连接的socket支持
- 简单易用 丰富的文档和示例 无附加依赖 Netty3.x依赖JDK5 Netty4.x依赖JDK6
- 性能 高吞吐低延迟 更少的资源消耗 最小的内存拷贝消耗
- 安全 完善的SSL/TLS StartTLS支持
- 社区 开源免费，维护更新快 社区活跃

### 线程模型简介

#### 传统I/O模型

- 传统IO模型采用阻塞的模式获取数据的输入
- 每一个连接需要单独的线程进行数据读取，业务处理和数据返回
- 存在的问题在于当并发数量巨大的时候，会产生非常多的线程，对资源的消耗大，线程阻塞当无数据可读线程闲置，造成资源的浪费

![img](https://upload-images.jianshu.io/upload_images/11345047-bdee5b09bf194782.png?imageMogr2/auto-orient/strip|imageView2/2/w/1014/format/webp)

#### Reactor模型

IO复用模型，多连接共享一个阻塞对象，应用程序只需要在一个阻塞对象中等待，无需阻塞等待所有连接。当某连接有数据可以处理的时候，操作系统通知应用程序进一步业务处理，线程从阻塞中返回。

核心思想就是多个关注的IO事件注册到一个多路复用器(Reactor)中，Reactor在一个单独的线程中运行,负责监听分发事件，一旦有某一种IO事件触发，将事件分发到指定的事件处理器(Handler)中，Handler订阅到关心的IO实践之后进行后续的业务处理。

![image-20200624153120416](C:\Users\denglw\AppData\Roaming\Typora\typora-user-images\image-20200624153120416.png)

Reactor模型三种模型实现

- 单Reactor单线程模式

![image-20200624153554779](C:\Users\denglw\AppData\Roaming\Typora\typora-user-images\image-20200624153554779.png)

1. 类似传统NIO模式Reactor相当于NIO中的Selector
2. reactor监听客户端的连接请求，有dispatch进行分发
3. 一旦有连接请求建立，由acceptor接受处理连接请求，创建一个handler对象来进行连接之后的业务处理。
4. 如果不是建立连接事件，reactor分发连接时对应的handler进行处理
5. handler 完成数据读取 -- 业务处理 -- 返回的流程
6. 优点：模型简单，不存在多线程，没有线程切换，线程通信竞争问题
7. 缺点： 性能有限，因为只有一个线程，无法发挥多核CPU的优势。handler在处理某一个连接业务的时候无法处理其他连接事件。可靠性低，线程意外终止，死循环，会直接导致整个系统不可用

- 单Reactor多线程模式

![image-20200624154657116](C:\Users\denglw\AppData\Roaming\Typora\typora-user-images\image-20200624154657116.png)



1. reactor监听用户的请求，如果是建立连接请求则通过acceptor处理，创建一个handler来处理连接之后的各种事件，如果不是建立连接请求reactor通过dispatch分发到连接建立是acceptor创建的handler中进行后续的处理。
2. handler只负责处理数据读取和数据响应的事件，不做具体的业务处理，当read到数据的时候直接分发到worker线程中的某一个worker线程进行业务处理
3. worker线程池分发独立的线程处理具体的业务将结果返回给handler
4. handler拿到响应结果之后，把结果send返回给调用的client端
5. 优点： 利用多核CPU的优势，加快了业务处理的速度
6. 缺点： reactor存在单点问题，reactor是系统的核心如何所有事件监听和分发，且在单线程中运行，如果reactor在高并发情况下容易处理性能瓶颈，且如果reactor运行异常则导致整个系统不可用

- 多Reactor多线程模式 Netty基于此模式

  ![image-20200624155810388](C:\Users\denglw\AppData\Roaming\Typora\typora-user-images\image-20200624155810388.png)

1. MainReactor监听所有Client的连接事件，MainReactor下面可以多个SubReactor，收到连接事件之后acceptor进行处理连接事件，MainReactor将连接dispatch给SubReactor

2. SubReactor收到MainReactor的连接事件，创建一个Handler来处理读写数据操作，当handler接受到数据的时候分发给woker线程池

3. woker线程池分配一个具体的线程来进行业务处理

4. woker线程处理完具体业务逻辑之后返回给handler

5. handler收到具体的处理结果直接返回给client

6. 优点 MainReactor只负责接受客户端的连接，SubReactor 负责处理后面的业务

   由SubReactor中的handler直接将结果返回给Client

7. 缺点 编程复杂度高

### Netty原理模型

![image-20200624171201551](C:\Users\denglw\AppData\Roaming\Typora\typora-user-images\image-20200624171201551.png)

1. Netty有两组线程池分别是BossGroup用于处理客户端连接，WokerGroup用于处理网络读写业务处理
2. BossGroup和WokerGroup类型都是NioEventLoopGroup 事件循环组
3. NioEventLoopGroup是一个事件循环组，管理了多个事件循环NioEventLoop
4. NioEventLoop是一个不断循环处理任务的线程，每一个NioEventLoop都有一个Selector用于监听绑定其socket上关心的事件 每一个NioEventLoop对应一个线程
5. BossGroup 下的 EventLoop循环三步
   1. 轮询监听accept事件
   2. 处理accept事件，与client建立连接，生成一个NioSocketChannel并注册到WorkerGroup 中某一个woker的NioEventLoop的Selector中
   3. 处理任务队列中的任务 runAllTask
6. WokerGroup 下的EventLoop操作
   1. 轮询read 和 write事件
   2. 在对应的NioSocketChannel中处理read write事件
   3. 处理任务队列中的任务 runAllTask
7. Woker Group中的 NioEventLoop 在处理业务时候，会使用pipeline，pipeline中包含了channel，通过pipeline获取到channel，pipeline中绑定许多的处理器handler进行具体的业务处理

### 代码演示

- 核心API汇总
  - 服务端
    1. 创建2个线程组 NioEventLoopGroup
    2. 创建服务端配置对象ServerBootstrap
    3. 配置Netty2大线程组 .group(bossGroup,wokerGroup)
    4. 配置线程组channel的类型 .channel(NioServerSocketChannel.class)
    5. 线程组的属性配置 option() 和 childOption() 分别对应bossGroup 和 workerGroup
    6. 添加线程组处理器.childHandler() 添加pipeline处理器
    7. 调用pipeline的addLast 或者 addXXX方法添加处理器
    8. boot.bind(port).sync() 绑定端口启动 返回一个ChannelFuture
    9. future 调用 future.channel().closeFuture().sync() 将主线程阻塞，一直等到有channel关闭事件之后再向下执行
    10. EventLoopGroup的shutdownGracefully() 线程组的优雅关闭
  - 处理器
    1. ChannelInboundHandlerAdapter 入栈适配器中的几个监听方法
    2. ChannelHandlerContext中的writeAndFlush写入数据并刷新
    3. Unpooled.copiedBuffer(byte[]) 构建ByteBuf用于数据传输
    4. ChannelHandlerContext.alloc().buffer() 另一种构建ByteBuf方法

```java
// 创建服务端
public class SimpleNettyServerDemo {

    public static void main(String[] args){
        // 创建boosGroup
        NioEventLoopGroup boosGroup = new NioEventLoopGroup(1);
        // 创建workerGroup 参数表示具体线程数，一个EventLoopGroup中有多少个EventLoop 默认不传参数CPU核数*2
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        // 服务端配置对象
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boosGroup, workerGroup) // 传入 bossGroup 和worker Group
                .channel(NioServerSocketChannel.class) //使用NioSocketChannel的类型 这里使用NioServerSocketChannel作为通道的实现
                .option(ChannelOption.SO_BACKLOG, 128) // 设置boosGroup线程队列得到连接个数
                .childOption(ChannelOption.SO_KEEPALIVE, true)//设置workerGroup 保持活动连接状态
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // 添加处理器
                        socketChannel.pipeline().addLast(new SimpleNettyServerHandler());
                    }
                });
        System.out.println("服务器准备完毕");
        // 启动服务绑定端口
        ChannelFuture future = null;
        try {
            future = serverBootstrap.bind(6666).sync();
            // 关闭通道监听
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            // 优雅关闭boosGroup 和 workerGroup
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
```

```java
// 服务端处理器
public class SimpleNettyServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * 当连接建立成功之后的操作
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端" + ctx.pipeline().channel().remoteAddress() + "建立连接成功");
    }

    /**
     * 有数据读取的事件
     *
     * @param ctx 上下文对象 包产了pipeLine pipeLine包含了Channel Channel包含了客户端的信息
     * @param msg msg就是客户端发送的信息
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 强制转换成一个ByteBuf 这里的ByteBuf是Netty原生的 而不是NIO中的ByteBuffer
        ByteBuf buf = (ByteBuf) msg;
        Channel channel = ctx.pipeline().channel();
        System.out.println("收到客户端" + channel.remoteAddress() + "的消息==>" + buf.toString(Charset.forName("UTF-8")));
    }

    /**
     * 数据读取完毕事件
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 服务端给客户端写数据 使用Netty的Unpooled 非池化类构建一个ByteBuf
        ctx.writeAndFlush(Unpooled.copiedBuffer("收到了你的消息客户端".getBytes()));
    }

    /**
     * 出现异常事件
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 出现异常关闭连接 打印错误栈
        ctx.close();
        cause.printStackTrace();
    }
}
```

- 客户端核心API
  - 客户端
    1. 客户端配置对象Bootstrap 区别于服务端的ServerBootstrap
    2. 其他API与服务端类似group(工作线程组)
    3. channel 配置通道类型
    4. .handler添加处理器

```java
// 构建客户端
public class SimpleNettyClientDemo {

    public static void main(String[] args) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class) // 这是客户端通道类型为NioSocketChannel
        .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new SimpleNettyClientHandler());
            }
        });

        try {
            ChannelFuture future = bootstrap.connect("localhost", 6666).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
        }

    }
}
```

```java
// 客户端处理器
public class SimpleNettyClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 使用上下文对象创建一个ByteBuf内存管理器
        ByteBuf buffer = ctx.alloc().buffer();
        // 调用ByteBuf中的writeBytes方法将byte数据写入buf
        buffer.writeBytes("helloServer!".getBytes());
        ctx.writeAndFlush(buffer);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("收到服务端的回信: " + buf.toString(Charset.defaultCharset()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }
}
```

- 补充

  - ChannelHandlerContext.writeAndFlush 和 ChannelHandlerContext.channel.writeAndFlush区别

    ChannelHandlerContext.writeAndFlush 从当前context找到上一个outBound，从后向前调用write

    ChannelHandlerContext.channel.writeAndFlush 从tail到head 调用每一个outBound 的 write