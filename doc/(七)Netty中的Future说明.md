### Netty Future解析

Netty中的Future是一个异步调用的返回值，Netty中的IO操作是异步的，包括bind write connect等方法会返回一个ChannelFuture 

ChannelFuture 继承 Netty原生的Future 而Netty原生的Future继承自JUC的Future

```java
public interface Future<V> extends java.util.concurrent.Future<V>
```

通过在ChannelFuture上添加监听方法，在真正执行完毕之后的进入回调方法进行下一步的处理。

- Future常用API
  1. isDone 当前操作是否完成
  2. isSuccess 当前操作是否成功 完成不一定成功，成功一定是完成了
  3. isCancelled 当前操作是否取消 isDone为true
  4. cause 如果失败了返回原因
  5. addListener 注册监听器，当操作完成idDone=true，通知指定监听器进行下一步业务操作
  6. sync 阻塞主进程等待关闭事件

```java
// 源码中对 future各种情况的说明
 *                                      +---------------------------+
 *                                      | Completed successfully    |
 *                                      +---------------------------+
 *                                 +---->      isDone() = true      |
 * +--------------------------+    |    |   isSuccess() = true      |
 * |        Uncompleted       |    |    +===========================+
 * +--------------------------+    |    | Completed with failure    |
 * |      isDone() = false    |    |    +---------------------------+
 * |   isSuccess() = false    |----+---->      isDone() = true      |
 * | isCancelled() = false    |    |    |       cause() = non-null  |
 * |       cause() = null     |    |    +===========================+
 * +--------------------------+    |    | Completed by cancellation |
 *                                 |    +---------------------------+
 *                                 +---->      isDone() = true      |
 *                                      | isCancelled() = true      |
 *                                      +---------------------------+
```



### 代码演示

```java
        try {
            future = serverBootstrap.bind(7777).sync();
            // 添加端口绑定事件监听
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        System.out.println("server 启动 端口7777成功");
                    }else{
                        System.out.println("server 启动 端口7777失败");
                    }
                }
            });
            // 添加关闭事件 事件监听
            ChannelFuture closeFuture = future.channel().closeFuture().sync();
            closeFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        System.out.println("server 成功关闭!");
                    }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
```





