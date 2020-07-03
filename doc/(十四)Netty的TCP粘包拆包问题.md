TCP是面向连接和流的协议，操作系统在TCP发送数据的时候会有一个缓冲区，在发送大数据块时将一个包拆分成多次较小的数据发送（拆包），在发送小数据包的时候且间隔时间较短，会将多个数据块合并到一个数据包中合并（粘包）。因为面向流通信无消息保护边界，导致接收端无法清晰的分辨出一个完整的数据包。

TCP的粘包拆分会导致几种情况的出现

假定发送数据包A B

- AB数据包都达到缓冲区大小，会分别发送2个单独的数据包发送
- AB请求时间较短且AB的数据量较小，会将AB的数据合并和一个数据包发送出去
- A数据大B数据小 A拆分为 A_1和 A_2，其中A_1单独数据包发送，A_2的部分数据会跟B的数据包部分或者全部数据合并发送

#### 解决方案

解决TCP粘包拆包的关键在于接收端每次收到消息长度问题或者分辨出何为一个完整的数据块，就不会出现接收端读多了或者读少了的问题。

1. 定长消息，客户端每次发送数据包的时候，长度固定如1024，接收端在长度不足1024则填充

   Netty提供的FixedLengthFrameDecoder用于使用固定长度来解决粘包拆包问题，每次读取一个固定的长度如何不足则补足。但是Netty只提供了解码器，需要自己实现固定长度的编码器，编码时只需要实现不足长度的补足即可。

   ```java
   // 服务端实现
   public class TCPFixLengthServerDemo {
   
       public static void main(String[] args) {
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
                           ChannelPipeline pipeline = socketChannel.pipeline();
                           // 自定义的定长编码器
                           pipeline.addLast(new MyFixedLengthFrameEncoder(512));
                           // StringDecoder放在 FixLengthFrameDecode之前因为先将String decode之后再进行长度decode才能保证数据的正确传输
                           pipeline.addLast(new StringDecoder());
                           pipeline.addLast(new FixedLengthFrameDecoder(512));
                           pipeline.addLast(new TCPNettyServerHandler());
                       }
                   });
           // 启动服务绑定端口
           ChannelFuture future = null;
           try {
               future = serverBootstrap.bind(6666).sync();
               // 关闭通道监听
               future.channel().closeFuture().sync();
               System.out.println(123123);
   
           } catch (InterruptedException e) {
               e.printStackTrace();
           } finally {
               // 优雅关闭boosGroup 和 workerGroup
               boosGroup.shutdownGracefully();
               workerGroup.shutdownGracefully();
           }
       }
   
   }
   ```

   ```java
   // 长度编码填充
   public class MyFixedLengthFrameEncoder extends MessageToByteEncoder<String> {
   
       private int length;
   
       public MyFixedLengthFrameEncoder(int length) {
           this.length = length;
       }
   
       @Override
       protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
           // 如果长度大于 拆分
           while (msg.length() > length) {
               String sendMsg = msg.substring(0, length);
               ctx.channel().writeAndFlush(sendMsg);
               msg = msg.substring(length);
           }
           // 如果长度不足补全
           if (msg.length() < length) {
               msg = appendToLength(msg);
           }
           out.writeBytes(msg.getBytes());
       }
   
       private String appendToLength(String msg) {
           StringBuilder builder = new StringBuilder(msg);
           for (int i = 0; i < length - msg.length(); i++) {
               builder.append(" ");
           }
   
           return builder.toString();
       }
   
   }
   ```

   ```java
   public class TCPNettyServerHandler extends SimpleChannelInboundHandler<String> {
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
   
   
       @Override
       protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
           System.out.println("收到客户端" + ctx.channel().remoteAddress() + "的消息长度为" + msg.length() + "==>" + msg );
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
           ctx.channel().writeAndFlush("收到了你的消息客户端");
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
       }
   }
   ```

   ```java
   public class TCPNettyClientDemo {
   
       public static void main(String[] args) {
           NioEventLoopGroup workerGroup = new NioEventLoopGroup();
   
           Bootstrap bootstrap = new Bootstrap();
           bootstrap.group(workerGroup)
                   .channel(NioSocketChannel.class) // 这是客户端通道类型为NioSocketChannel
                   .handler(new ChannelInitializer<SocketChannel>() {
                       @Override
                       protected void initChannel(SocketChannel socketChannel) throws Exception {
                           ChannelPipeline pipeline = socketChannel.pipeline();
                           // 自定义的定长编码器
                           pipeline.addLast(new MyFixedLengthFrameEncoder(512));
                           // StringDecoder放在 FixLengthFrameDecode之前因为先将String decode之后再进行长度decode才能保证数据的正确传输
                           pipeline.addLast(new StringDecoder());
                           pipeline.addLast(new FixedLengthFrameDecoder(512));
                           pipeline.addLast(new TCPNettyClientHandler());
                       }
                   });
   
           try {
               ChannelFuture future = bootstrap.connect("localhost", 6666).sync();
               future.channel().closeFuture().sync();
           } catch (InterruptedException e) {
               e.printStackTrace();
           } finally {
               workerGroup.shutdownGracefully();
           }
       }
   }
   ```

   ```java
   public class TCPNettyClientHandler extends SimpleChannelInboundHandler<String> {
   
       @Override
       public void channelActive(ChannelHandlerContext ctx) throws Exception {
           ctx.channel().writeAndFlush("helloServer");
       }
   
       @Override
       protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
           System.out.println("收到服务端的长度为" + msg.length() + "的回信: " + msg);
       }
   
       @Override
       public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
           ctx.close();
       }
   }
   ```

   

2. 每个完成数据块添加收尾分隔符，通过分隔符来确定一个完整的数据块

   LineBasedFrameDecoder与DelimiterBasedFrameDecoder

   Netty提供用于添加分隔符来判断数据块的类

   LineBasedFrameDecoder用\n或者\r\n分割 DelimiterBasedFrameDecoder使用用户自定义的分隔符 下面用DelimiterBasedFrameDecoder为例

   ```java
   // server端
   public class DelimiterServerDemo {
   
       public static void main(String[] args) {
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
                           ChannelPipeline pipeline = socketChannel.pipeline();
                           String delimiter = "$$";
                           // 自定义分隔符编码器
                           pipeline.addLast(new MyDelimiterNettyEncoder(delimiter));
                           // 被按照$$进行分隔，1024指的是分隔的最大长度，读取到1024个字节的数据之后，若还是未读取到分隔符，则舍弃当前数据段
                           // 放在StringDecoder之前 先将分隔符去掉
                           pipeline.addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer(delimiter.getBytes())));
                           // 将分隔之后的字节数据转换为字符串 会传给后面的 SimpleChannelInboundHandler<String>
                           pipeline.addLast(new StringDecoder());
                           pipeline.addLast(new TCPNettyServerHandler());
                       }
                   });
           // 启动服务绑定端口
           ChannelFuture future = null;
           try {
               future = serverBootstrap.bind(6666).sync();
               // 关闭通道监听
               future.channel().closeFuture().sync();
               System.out.println(123123);
   
           } catch (InterruptedException e) {
               e.printStackTrace();
           } finally {
               // 优雅关闭boosGroup 和 workerGroup
               boosGroup.shutdownGracefully();
               workerGroup.shutdownGracefully();
           }
       }
   
   }
   ```

   ```java
   // 编码器
   public class MyDelimiterNettyEncoder extends MessageToByteEncoder<String> {
       private String delimiter;
   
       public MyDelimiterNettyEncoder(String delimiter) {
           this.delimiter = delimiter;
       }
   
       @Override
       protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
           // 在响应的数据后面添加分隔符
           ctx.channel().writeAndFlush(Unpooled.wrappedBuffer((msg + delimiter).getBytes()));
       }
   }
   ```

   ```java
   // 客户端
   public class DelimiterNettyClientDemo {
   
       public static void main(String[] args) {
           NioEventLoopGroup workerGroup = new NioEventLoopGroup();
   
           Bootstrap bootstrap = new Bootstrap();
           bootstrap.group(workerGroup)
                   .channel(NioSocketChannel.class) // 这是客户端通道类型为NioSocketChannel
                   .handler(new ChannelInitializer<SocketChannel>() {
                       @Override
                       protected void initChannel(SocketChannel socketChannel) throws Exception {
                           ChannelPipeline pipeline = socketChannel.pipeline();
                           String delimiter = "$$";
                           // 自定义分隔符编码器
                           pipeline.addLast(new MyDelimiterNettyEncoder(delimiter));
                           // 被按照$$进行分隔，1024指的是分隔的最大长度，读取到1024个字节的数据之后，若还是未读取到分隔符，则舍弃当前数据段
                           // 放在StringDecoder之前 先将分隔符去掉
                           pipeline.addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer(delimiter.getBytes())));
                           // 将分隔之后的字节数据转换为字符串 会传给后面的 SimpleChannelInboundHandler<String>
                           pipeline.addLast(new StringDecoder());
                           pipeline.addLast(new TCPNettyClientHandler());
                       }
                   });
   
           try {
               ChannelFuture future = bootstrap.connect("localhost", 6666).sync();
               future.channel().closeFuture().sync();
           } catch (InterruptedException e) {
               e.printStackTrace();
           } finally {
               workerGroup.shutdownGracefully();
           }
       }
   }
   ```

3. 信息上包装长度信息 （推荐）

   LengthFieldBasedFrameDecoder与LengthFieldPrepender

   这两个编解码器结合起来使用可以在消息发送前添加一个长度消息头，接收方接受到消息之后去根据长度消息头上面的长度信息去读取具体的消息，并去除长度消息头

   LengthFieldPrepender 长度消息头添加的编码器

   LengthFieldBasedFrameDecoder 解析长度消息头的解码器

   ```java
   // server 
   public class LengthFieldServerDemo {
   
       public static void main(String[] args) {
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
                           ChannelPipeline pipeline = socketChannel.pipeline();
                           /**
                            * 具体场景参考 https://blog.csdn.net/u010853261/article/details/55803933
                            * int maxFrameLength, 每个包所能传递的最大数据包大小
                            * int lengthFieldOffset, 长度字段在字节码中的偏移量
                            * int lengthFieldLength, 长度字段所占用的字节长度
                            * int lengthAdjustment, 不仅包含有消息头和消息体的数据进行消息头的长度的调整，这样就可以只得到消息体的数据，这里的lengthAdjustment指定的就是消息头的长度
                            * int initialBytesToStrip 长度字段在消息头中间的情况，可以通过initialBytesToStrip忽略掉消息头以及长度字段占用的字节
                            */
                           pipeline.addLast(new LengthFieldBasedFrameDecoder(1024,0,2,0,2));
                           // LengthFieldPrepender是一个编码器，主要是在响应字节数据前面添加字节长度字段 这里的长度2需要跟LengthFieldBaseFrameDecoder中占用字节长度一致 由于长度字段在起始位置并且长度为2，所以将initialBytesToStrip设置为2
                           pipeline.addLast(new LengthFieldPrepender(2));
                           // 这里需要添加StringEncoder和StringDecoder后面的handler写String才能正常接到
                           pipeline.addLast(new StringEncoder());
                           pipeline.addLast(new StringDecoder());
                           pipeline.addLast(new TCPNettyServerHandler());
                       }
                   });
           // 启动服务绑定端口
           ChannelFuture future = null;
           try {
               future = serverBootstrap.bind(6666).sync();
               // 关闭通道监听
               future.channel().closeFuture().sync();
               System.out.println(123123);
   
           } catch (InterruptedException e) {
               e.printStackTrace();
           } finally {
               // 优雅关闭boosGroup 和 workerGroup
               boosGroup.shutdownGracefully();
               workerGroup.shutdownGracefully();
           }
       }
   
   }
   ```

   ```java
   // 客户端
   public class LengthFieldNettyClientDemo {
   
       public static void main(String[] args) {
           NioEventLoopGroup workerGroup = new NioEventLoopGroup();
   
           Bootstrap bootstrap = new Bootstrap();
           bootstrap.group(workerGroup)
                   .channel(NioSocketChannel.class) // 这是客户端通道类型为NioSocketChannel
                   .handler(new ChannelInitializer<SocketChannel>() {
                       @Override
                       protected void initChannel(SocketChannel socketChannel) throws Exception {
                           ChannelPipeline pipeline = socketChannel.pipeline();
                           /**
                            * 具体场景参考 https://blog.csdn.net/u010853261/article/details/55803933
                            * int maxFrameLength, 每个包所能传递的最大数据包大小
                            * int lengthFieldOffset, 长度字段在字节码中的偏移量
                            * int lengthFieldLength, 长度字段所占用的字节长度
                            * int lengthAdjustment, 不仅包含有消息头和消息体的数据进行消息头的长度的调整，这样就可以只得到消息体的数据，这里的lengthAdjustment指定的就是消息头的长度
                            * int initialBytesToStrip 长度字段在消息头中间的情况，可以通过initialBytesToStrip忽略掉消息头以及长度字段占用的字节
                            */
                           pipeline.addLast(new LengthFieldBasedFrameDecoder(1024,0,2,0,2));
                           // LengthFieldPrepender是一个编码器，主要是在响应字节数据前面添加字节长度字段 这里的长度2需要跟LengthFieldBaseFrameDecoder中占用字节长度一致 由于长度字段在起始位置并且长度为2，所以将initialBytesToStrip设置为2
                           pipeline.addLast(new LengthFieldPrepender(2));
                           // 这里需要添加StringEncoder和StringDecoder后面的handler写String才能正常接到
                           pipeline.addLast(new StringEncoder());
                           pipeline.addLast(new StringDecoder());
                           pipeline.addLast(new TCPNettyClientHandler());
                       }
                   });
   
           try {
               ChannelFuture future = bootstrap.connect("localhost", 6666).sync();
               future.channel().closeFuture().sync();
           } catch (InterruptedException e) {
               e.printStackTrace();
           } finally {
               workerGroup.shutdownGracefully();
           }
       }
   }
   ```

4. 自定义协议

   通过自定一个协议来表明消息的长度和内容，思想跟LengthFieldBasedFrameDecoder与LengthFieldPrepender一直，自定义的更加灵活可以添加更多的其他信息字段

```java
// 自定义的协议
public class MyTcpMessage {

    private int length;

    private byte[] content;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
```

```java
// encoder
public class MyTcpMessageEncoder extends MessageToByteEncoder<MyTcpMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, MyTcpMessage msg, ByteBuf out) throws Exception {
        // 写入长度
        out.writeInt(msg.getLength());
        // 写入具体的数据
        out.writeBytes(msg.getContent());
    }
}
// decoder
public class MyTcpMessageDecoder extends ByteToMessageDecoder {

    // 更消息安全的解码器参考https://www.cnblogs.com/sidesky/p/6913109.html
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readInt();
        MyTcpMessage msg = new MyTcpMessage();
        msg.setLength(length);
        byte[] data = new byte[length];
        ByteBuf buf = in.readBytes(data);
        msg.setContent(data);
        out.add(msg);
    }
}
```

```java
//server 
public class MyTcpMessageServerDemo {

    public static void main(String[] args) {
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
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new MyTcpMessageEncoder());
                        pipeline.addLast(new MyTcpMessageDecoder());
                        pipeline.addLast(new MyTcpMessageServerHandler());
                    }
                });
        // 启动服务绑定端口
        ChannelFuture future = null;
        try {
            future = serverBootstrap.bind(6666).sync();
            // 关闭通道监听
            future.channel().closeFuture().sync();
            System.out.println(123123);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 优雅关闭boosGroup 和 workerGroup
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
// server handler
public class MyTcpMessageServerHandler extends SimpleChannelInboundHandler<MyTcpMessage> {
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


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MyTcpMessage msg) throws Exception {
        System.out.println("收到客户端" + ctx.channel().remoteAddress() + "的消息长度为" + msg.getLength() + "==>" + new String(msg.getContent()));
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
        MyTcpMessage msg = new MyTcpMessage();
        msg.setContent("收到了你的消息客户端".getBytes());
        msg.setLength(msg.getContent().length);
        ctx.channel().writeAndFlush(msg);
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
    }
}
```

```java
// client
public class MyTcpMessageClientDemo {

    public static void main(String[] args) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class) // 这是客户端通道类型为NioSocketChannel
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new MyTcpMessageEncoder());
                        pipeline.addLast(new MyTcpMessageDecoder());
                        pipeline.addLast(new MyTcpMessageClientHandler());
                    }
                });

        try {
            ChannelFuture future = bootstrap.connect("localhost", 6666).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}

// client handler

public class MyTcpMessageClientHandler extends SimpleChannelInboundHandler<MyTcpMessage> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        MyTcpMessage msg = new MyTcpMessage();
        msg.setContent("helloServer".getBytes());
        msg.setLength(msg.getContent().length);
        ctx.channel().writeAndFlush(msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MyTcpMessage msg) throws Exception {
        System.out.println("收到服务端的长度为" + msg.getLength() + "的回信: " + new String(msg.getContent()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
```

