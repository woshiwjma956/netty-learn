### Netty任务队列

Netty的任务队列的task的使用在EventLoop中最后一个步骤是执行所有的task，Netty在每一个事件循序中维护了一个任务队列，客户端可以向队列中添加普通任务，定时任务，非当前Reactor线程的Channel方法。

创建Server和Client的代码跟之前一样，这里只列出了handler部分

- 普通任务 获取ctx的eventLoop 提交一个runnable 即可

```java
// 普通任务
public class NettyTaskServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("receive msg from client : " + buf.toString(Charset.defaultCharset()));
        // 提交一个异步任务到上下文的eventLoop的线程池中
        ctx.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(5);
                    System.out.println("sync after 5 seconds  runnable task ... ");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello client!".getBytes()));
        System.out.println("server go on...说明提交一个异步的任务");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}
```

-  定时和延时任务

```java
// 定时任务
public class NettyTaskServerHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("receive msg from client : " + buf.toString(Charset.defaultCharset()));
        /**
         * 启动一个延时任务
         * 参数2 延时时间
         * 参数3 延时时间单位
         */
        ctx.channel().eventLoop().schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("启动一个延时3秒的任务...");
            }
        }, 3, TimeUnit.SECONDS);
        /**
         * 启动一个定时任务
         * 参数2: delay 延时时长
         * 参数3: period 间隔时长
         * 参数4: timeUnit 时间单位
         */
        ctx.channel().eventLoop().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("启动一个延时3秒 每1秒执行一次定时任务..");
            }
        }, 3, 1, TimeUnit.SECONDS);
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello client!".getBytes()));
        System.out.println("server go on...说明提交一个异步的任务");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
/**
receive msg from client :  hello server !
server go on...说明提交一个异步的任务
启动一个延时任务...
启动一个延时3秒 每1秒执行一次定时任务..
启动一个延时3秒 每1秒执行一次定时任务..
启动一个延时3秒 每1秒执行一次定时任务..
启动一个延时3秒 每1秒执行一次定时任务..
启动一个延时3秒 每1秒执行一次定时任务..
启动一个延时3秒 每1秒执行一次定时任务..
/*
```

- 说明

  NioEventLoop采用串行化设计，从消息 读取 - 解码 - 处理 - 编码 - 发送

  NioEventLoopGroup 中维护了多个NioEventLoop

  NioEventLoop中维护一个Selector 和 一个taskQueue

  NioEventLoop下的selector 可以监听多个NioChannel

  NioChannel只能注册绑定到唯一一个NioEventLoop的Selector中

  NioChannel都会绑定一个自己的ChannelPipeline 通过Pipeline也可以反向得到改Channel

  