### Netty空闲检测Handler

io.netty.handler.timeout.IdleStateHandler Netty原生的空闲检测处理器

如果出现读写空闲会发送一个IdelStateEvent到下一个handler，在下一个handler中的userEventTriggered方法中捕获进行具体的事件逻辑处理

```java
// 服务端代码
public class NettyIdelHeatBeatServerDemo {

    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(2);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            /**
                             * 添加一个ideal 空闲监听
                             * 参数1 readerIdleTime 读空闲时间
                             * 参数2 writerIdleTime 写空闲时间
                             * 参数3 allIdleTime 读写空闲时间
                             * 参数4 TimeUnit 时间单位
                             * 出现读写空间之后 IdelStateHandler会传给下一个handler一个IdelStateEvent事件
                             * 下一个handler在userEventTriggered中接受
                             */
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new IdleStateHandler(3,5,7, TimeUnit.SECONDS));
                            pipeline.addLast(new IdelEventHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(4444).sync();
            channelFuture.channel().closeFuture().sync();
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
// 自定义的idel事件处理器
public class MyIdleHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + " is ready...");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println(evt.getClass());
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;

            switch (event.state()) {
                case READER_IDLE:
                    System.out.println("read idle..");
                    break;
                case WRITER_IDLE:
                    System.out.println("write idle");
                    break;
                case ALL_IDLE:
                    System.out.println("read and write idle");
                    break;
            }

        }
    }
}
```

```java
// 客户端测试代码
public class NettyIdelHeatBeatClientDemo {

    public static void main(String[] args) throws Exception {
        NioEventLoopGroup work = new NioEventLoopGroup(2);

        try {
            Bootstrap bootstrap = new Bootstrap();
            ChannelFuture channelFuture = bootstrap.group(work)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    ctx.writeAndFlush("hello Server");
                                }
                            });
                        }
                    })
                    .connect("localhost", 4444);
            channelFuture.sync();
            channelFuture.channel().closeFuture().sync();
            //}
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            work.shutdownGracefully();
        }

    }
}
```

