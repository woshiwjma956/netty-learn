### 需求

基于Netty实现一个群聊系统，要求客户端感知其他用户的上线下线，能接受到其他客户端发送的信息。实现思路，服务端用一个全局容器管理一个Channel组，在客户端连接的时候加入组中并通知组内其他成员，反之客户端断开连接从组内移除并通知组内其他成员。转发通过服务端转发给组内给非当前消息发送方的所有组员。

```java
// 服务端启动类
public class NettyGroupChartServer {

    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new NettyGroupChatServerHandler());
                    }
                });
        try {
            ChannelFuture future = serverBootstrap.bind(4444).sync();
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
// 服务端处理器
public class NettyGroupChatServerHandler extends SimpleChannelInboundHandler<String> {

    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + " is online ...!");
        channelGroup.writeAndFlush(ctx.channel().remoteAddress() + " is online ...!");
        channelGroup.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + " is offline ...!");
        channelGroup.remove(ctx.channel());
        channelGroup.writeAndFlush(ctx.channel().remoteAddress() + " is offline ...!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel self = ctx.channel();
        // 不是自己
        ChannelMatcher matcher = channel -> channel != self;
        channelGroup.writeAndFlush(self.remoteAddress() + " say: " + msg, matcher);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
```

```java
// 客户端启动类
public class NettyGroupChatClient {

    public static void main(String[] args) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(worker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new NettyGroupChatClientHandler());
                    }
                });

        try {
            ChannelFuture future = bootstrap.connect("localhost", 4444).sync();
            future.channel().closeFuture();
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String msg = scanner.nextLine();
                System.out.println("I say: " + msg);
                future.channel().writeAndFlush(msg);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            worker.shutdownGracefully();
        }
    }
}
```

```java
// 客户端处理类
public class NettyGroupChatClientHandler extends SimpleChannelInboundHandler<String> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
```

#### 核心要点

ChannelGroup类用于全局管理Channel组

也可以使用的handlerAdded和handlerRemoved表示建立连接的时候第一个执行