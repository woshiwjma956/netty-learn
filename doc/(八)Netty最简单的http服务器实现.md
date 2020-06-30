### 基于Netty实现一个最简单的Http服务器

- 核心API

  ```java
  // httpServerCodec是Netty提供的对Http的编解码器
  ch.pipeline().addLast("HttpCodec", new HttpServerCodec());
  // netty 使用httpCodeC编解码之后受到msg会是netty中的HttpRequest类型
  io.netty.handler.codec.http.HttpRequest
  // netty 使用HttpRespone 作为response 输出影响    
  io.netty.handler.codec.http.HttpResponse
  ```

### 代码演示

```java
// 核心就一个添加HttpServerCodec Netty自带的Http编解码器
public class NettyHttpServerDemo {

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
                        // httpServerCodec是Netty提供的对Http的编解码器
                        ch.pipeline().addLast("HttpCodec", new HttpServerCodec());
                        ch.pipeline().addLast(new NettyHttpServerHandler());
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
// handler
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {		// 判断是否是 Netty自身的HttpRequest 不是servlet的HttpRequest
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            System.out.println("请求URL: " + request.uri());
            System.out.println("请求Method: " + request.method().name());
            if (request.uri().equals("/favicon.ico")) {
                System.out.println("请求图片没有不响应");
            } else {
                ByteBuf content = ctx.alloc().buffer();
                content.writeBytes("send from server".getBytes());
                // 构建一个netty 的httpResponse写入 使用netty原生的HttpResponse
                DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
                resp.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain");
                resp.headers().set(HttpHeaderNames.CONTENT_LENGTH,content.readableBytes());
                ctx.writeAndFlush(resp);
            }

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
```

