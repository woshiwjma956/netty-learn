### 配置对象Bootstrap ServerBootStrap

Boostrap是Netty中的配置引导类，主要配置Netty的EventGroup及其选项和处理器等，串联Netty中的各种组件，Boostrap用于配置客户端，ServerBootStrap用于配置服务端。

#### 核心API

```java
// 服务端配置 bossGroup 和  workerGroup对象
public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup);
// 配置 客户端 channel 的选项
public <T> ServerBootstrap childOption(ChannelOption<T> childOption, T value);
// 配置业务处理器
public ServerBootstrap childHandler(ChannelHandler childHandler);
//  父类方法 配置服务端的通道实现
public B channel(Class<? extends C> channelClass);
// 父类方法 配置服务端的 channel属性
public <T> B option(ChannelOption<T> option, T value);
// 绑定 host 和 port
public ChannelFuture bind(String inetHost, int inetPort);
// Boostrap 连接 服务端方法
public ChannelFuture connect(String inetHost, int inetPort)
```

### 异步操作返回对象 Future ChannelFuture

Netty中的异步操作会返回一个Netty原生的Future对象继承自JUC的Future对象，通过添加监听的方式当异步操作结束之后会触发指定的监听器进行下一步操作。

#### 核心API

```java
Channel channel(); //返回当前正在进行IO操作的Channel
ChannelFuture sync();// 阻塞主线程等待异步操作结束 返回一个 future对象可以对其添加事件监听

```

### 通信组件Channel

1. Netty中负责网络通信的组件，用于网络IO操作
2. 可以获取当前网络连接通道状态 isOpen isActive isRegistered isWriteable isReadble
3. 可以获取网络连接配置参数 config方法 返回ChannelConfig对象包含了各种配置参数
4. 异步的IO操作 write read操作
5. 异步操作返回ChannelFuture 可以通过Future来对各种状态的回调进行不同的业务处理
6. 不同协议提供了不同的Channel类型 常见类型如下
   1. NioSocketChannel 客户端TCP socket
   2. NioServerSocketChannel 服务端TCP socket
   3. NioDatagramChannel UDP
   4. NioSctpChannel 客户端SCTP协议
   5. NioServerSctpChannel 服务端SCTP协议

### 业务处理器ChannelHandler

channelHandler是Netty中的业务处理器，负责处理IO事件或者拦截IO操作，处理完成之后发送pipeline中的下一个handler

常用的ChannelHandler子类

1. ChannelInBoundHandler 处理入栈IO事件
2. ChannelOutBoundHandler 处理出栈IO事件
3. ChannelInBoundHandlerAdapter和ChannelOutBoundHandlerAdapter 分别是入栈出栈适配器
4. 常用事件
   1. channelActivte 通道就绪事件
   2. channelInActivte 通道失效事件
   3. channelRead 通道有读IO操作事件
   4. channelReadComplete 通道读事件处理完毕事件
   5. exceptionCaught 出现异常事件
   6. channelRegistered和channelUnregistered 通道注册EventLoop的事件
   7. userEventTriggered 用户自定事件触发器

### 管道Pipeline ChannelPileline 管道处理器上下文ChannelHandlerContext

1. pipeline是一个channelHandler的集合，负责处理inBound和outBound的事件操作，贯穿整个Netty

2. channelPipeline实现了拦截器模式，用户可以控制事件的处理方式，以及各处理器的交互规则

3. 每一个channel中只有一个pipeline，一个pipeline中也对应了一个channel

4. pipeline维护了一个双向链表，由多个channelHandlerContext组成，ChannelHandlerContext对应了一个具体的ChannelHandler

5. 入栈事件会从head头部流向tail尾部，出栈会由tail尾部流向head头部，两种类型的处理器相互独立

6. Pipeline常用方法addLast(ChannelHandler) addFirst(ChannelHandler)

7. ChannelHandlerContext对象维护了一个具体的ChannelHandler，同时也绑定了pipeline和channel的信息，可以对pipeline上其他handler进行通信

8. ChannelHandlerContext常用方法write writeAndFlush close

9. ChannelHandlerContext的write方法和Channel的write方法的区别

   1. ChannlerHandlerContext的write方法获取到pipeline中添加到当前handler之前第一个被找到的outBoundHandler的write方法写出，需要考虑添加顺序如果当前handler之前没有任何outBoundHandler则会直接写出

      ```java
      public class DiffBetweenCtxAndChannelWriteDemo {
      
          public static void main(String[] args) {
              NioEventLoopGroup bossGroup = new NioEventLoopGroup();
              NioEventLoopGroup workerGroup = new NioEventLoopGroup();
              ServerBootstrap bootstrap = new ServerBootstrap();
              bootstrap.group(bossGroup, workerGroup)
                      .channel(NioServerSocketChannel.class)
                      .option(ChannelOption.SO_BACKLOG, 128)
                      .childOption(ChannelOption.SO_KEEPALIVE, true)
                      .childHandler(new ChannelInitializer<SocketChannel>() {
                          @Override
                          protected void initChannel(SocketChannel ch) throws Exception {
                              // 核心处理handler的添加顺序
                              ch.pipeline().addLast(new HttpServerCodec());
                              ch.pipeline().addLast(new DiffBetweenCtxAndChannelWriteDemoOutBoundHandler());
                              ch.pipeline().addLast(new DiffBetweenCtxAndChannelWriteDemoInBoundHandler());
                              ch.pipeline().addLast(new DiffBetweenCtxAndChannelWriteDemoOutBoundHandler());
                          }
                      });
              try {
                  ChannelFuture future = bootstrap.bind(4444).sync();
                  future.channel().closeFuture().sync();
              } catch (InterruptedException e) {
                  e.printStackTrace();
              } finally {
                  bossGroup.shutdownGracefully();
                  workerGroup.shutdownGracefully();
              }
          }
      }
      ```

      

   ```java
   // ChannelHandlerContext的write 方法
   private void write(Object msg, boolean flush, ChannelPromise promise) {
       // 核心在于找到outBoundContext
           AbstractChannelHandlerContext next = findContextOutbound();
           final Object m = pipeline.touch(msg, next);
           EventExecutor executor = next.executor();
           if (executor.inEventLoop()) {
               if (flush) {
                   next.invokeWriteAndFlush(m, promise);
               } else {
                next.invokeWrite(m, promise);
               }
           } else {
               AbstractWriteTask task;
               if (flush) {
                   task = WriteAndFlushTask.newInstance(next, m, promise);
               }  else {
                   task = WriteTask.newInstance(next, m, promise);
               }
               safeExecute(executor, task, promise, m);
           }
       }
   
       private AbstractChannelHandlerContext findContextOutbound() {
           // 找到该handler之前所有handler，第一个被找到outBoundHandler用其写出
           AbstractChannelHandlerContext ctx = this;
           do {
               ctx = ctx.prev;
           } while (!ctx.outbound);
           return ctx;
       }
   ```
   
   2.Channel的write方法 会流过所有的handler进行处理，然后找到最后一个处理器之前的第一个outBound写出，不需要考虑添加handler的添加顺序
   
   ```java
   // 调用pipeline的方法   
       public ChannelFuture writeAndFlush(Object msg) {
           return pipeline.writeAndFlush(msg);
       }
   //  pipe会一直调用到最后一个tail的handler 然后与ChannelContextHandler类似调用write方法找到最后一个handler 之前的第一个outBoundHandler 写出
       public final ChannelFuture writeAndFlush(Object msg) {
           return tail.writeAndFlush(msg);
       }
   
   ```



### 配置属性ChannleOption

Netty中配置通道属性的对象

常用有ChannelOpiton.SO_BACKLOG 初始化服务器可连接队列大小

ChannelOption.SO_KEEPALIVE 保持活动连接状态

### 缓冲区构建工具Unpooled

Netty提供的构建缓冲区数据容器Buffer的工具

常用方法 Unpooled.copyBuffer(CharSequence str,Charset charset) 返回一个Netty的ByteBuf

Netty的ByteBuf类似Nio的Bytebuffer不完全一样是对NIO的Buffer功能的增强