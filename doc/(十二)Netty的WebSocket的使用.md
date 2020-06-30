### Netty使用WebSocket

#### 核心API

server需要添加的handler

1. HttpServerCodec  WebSocket基于HTTP 需要添加Http编解码
2. ChunkedWriteHandler 对大数据进行分段处理
3. HttpObjectAggregator 对HttpMessage进行聚合
4. WebSocketServerProtocolHandler WebSocket的处理
5. TextWebSocketFrame WebSocket传输使用的帧对象

```java
// 服务端
public class NettyWebsocketServerDemo {

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
                        ChannelPipeline pipeline = ch.pipeline();
                        // 基于Http协议 添加Http编解码器
                        pipeline.addLast(new HttpServerCodec());
                        // 已块方式写 添加chunkedWriteHandler处理器 大数据流的处理
                        pipeline.addLast(new ChunkedWriteHandler());
                        // http数据在传输过程中是分段传输,HttpObjectAggregator可以将多段复合，聚合成FullHttpRequest或FullHttpResponse
                        pipeline.addLast(new HttpObjectAggregator(1024 * 8));
                        // 添加webSocketServerProtocolHandler 数据以帧Frame形式传输
                        // 指定浏览器请求的路径为ws://localhost:port/hello
                        // WebSocket都是以帧传输不同数据类型对应的frame也不同
                        // WebSocketServerProtocolHandler主要作用是将http 升级与ws 通过状态码101保持长连接
                        pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));
                        pipeline.addLast(new MyNettyWebSocketServerHandler());

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
// 处理器 注意TextWebSocketFrame
public class MyNettyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String content = LocalDate.now() + "收到客户端" + ctx.channel().remoteAddress() + "信息: " + msg.text();
        System.out.println(content);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(content));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+" online...");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+" offline...");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<script type="text/javascript">
    if ("WebSocket" in window) {
        // 打开一个 web socket
        var ws = new WebSocket("ws://localhost:4444/hello");

        ws.onopen = function () {
            // Web Socket 已连接上，使用 send() 方法发送数据
            ws.send("连接WS成功");
        };

        ws.onmessage = function (evt) {
            var content = document.getElementById("text").value
            content = content + "\n" + evt.data
            document.getElementById("text").value = content
        };

        ws.onclose = function () {
            // 关闭 websocket
            alert("连接已关闭...");
        };
    } else {
        // 浏览器不支持 WebSocket
        alert("您的浏览器不支持 WebSocket!");
    }

    function send() {
        var text = document.getElementById("msg").value;
        if (ws.readyState === WebSocket.OPEN) {
            ws.send(text);
        }
        document.getElementById("msg").value = ""
    }
</script>
<body>
<form action=""></form>
<input id="msg" type="text">
<input id="btn" type="button" value="submit" onclick="send()">
<textarea id="text" style="width: 300px"></textarea>
</body>
</html>
```

