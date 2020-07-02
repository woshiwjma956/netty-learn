Netty本身提供了一些编解码器如StringDecoder StringEncoder OjectDecoder ObjectEncoder，可以序列化JAVA的POJO对象，底层使用的java的序列化技术，java序列化本身效率不高，体积大，所有由Google开源了一个新的解决方案Google Protocol Buffers。

Google Protocol Buffers 是一个轻量级高效的结构化数据存储格式，可以用于数据的序列化，非常适合用于在远程调用RPC中做数据存储和数据交换。跨平台，跨语言（客户端和服务器可以使用不同的语言进行通信），支持大多数主流语言。通过protobuf的编译器通过.proto文件自动生成java类。

语法参考官网

[ProtoBuf proto3]: https://developers.google.cn/protocol-buffers/docs/proto3#default

#### 基本使用

1. 创建一个proto文件 描述需要被序列化的类

   ```protobuf
   syntax = "proto3";
   
   
   // option配置java文件输出的class名 同时也是文件名
   option java_outer_classname="StudentPojo";
   option optimize_for=SPEED;//加快解析
   option java_package="com.ycorn.nettypractices.protobuf"; // 生成放在哪个包下
   
   // message 关键字用于管理一个结构化数据 类似 class
   // 在StudentPojo 类的里面生成pr一个Student的内部类 是真正发送的对象
   message Student{
       // 定义枚举 必须从0开始
       enum Gender {
           MALE = 0;
           FEMALE = 1;
       }
   
       int32 id = 1; // 有一个属性名为id 类型是32位int 1表示序列号不是值
       string name = 2;
       int32 age = 4;
       // 使用之前定义的枚举
       Gender gender=3;
   
   }
   ```

2. 使用proto编译器将.proto文件编译成java文件

   [编译器下载地址]: https://github.com/protocolbuffers/protobuf/releases/tag/v3.12.3

   ```bash
   protoc.exe xxx.proto --java_out=Dir
   ```

3. 编写服务端和客户端java代码

   添加goole protobuf 相关依赖

   ```xml
        <dependency>
               <groupId>com.google.protobuf</groupId>
               <artifactId>protobuf-java</artifactId>
               <version>3.6.1</version>
           </dependency>
   ```

   

   ```java
   public class ProtoBufServerDemo {
   
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
                           //解码器，通过Google Protocol Buffers序列化框架动态的切割接收到的ByteBuf
                           pipeline.addLast(new ProtobufVarint32FrameDecoder());
                           //服务器端接收的是客户端StudentPojo对象, 需要进行解码
                           pipeline.addLast(new ProtobufDecoder(StudentPojo.Student.getDefaultInstance()));
                           //Google Protocol Buffers编码器
                           pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                           //Google Protocol Buffers编码器
                           pipeline.addLast(new ProtobufEncoder());
                           pipeline.addLast(new ProtoBufServerHandler());
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
   public class ProtoBufServerHandler extends SimpleChannelInboundHandler<StudentPojo.Student> {
   
   
       @Override
       protected void channelRead0(ChannelHandlerContext ctx, StudentPojo.Student msg) throws Exception {
           System.out.println(String.format("收到客户端%s的信息: %s", ctx.channel().remoteAddress(), msg.toString()));
       }
   
   
       @Override
       public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
           StudentPojo.Student student = StudentPojo.Student.newBuilder().setId(1).setGender(StudentPojo.Student.Gender.MALE).setAge(30).build();
           ctx.channel().writeAndFlush(student);
       }
   
       @Override
       public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
           ctx.close();
       }
   }
   
   ```

   ```java
   // 客户端
   public class ProtoBufClientDemo {
   
       public static void main(String[] args) {
           NioEventLoopGroup worker = new NioEventLoopGroup();
           Bootstrap bootstrap = new Bootstrap();
   
           bootstrap.group(worker)
                   .channel(NioSocketChannel.class)
                   .handler(new ChannelInitializer<SocketChannel>() {
                       @Override
                       protected void initChannel(SocketChannel ch) throws Exception {
                           ChannelPipeline pipeline = ch.pipeline();
                           //解码器，通过Google Protocol Buffers序列化框架动态的切割接收到的ByteBuf
                           pipeline.addLast(new ProtobufVarint32FrameDecoder());
                           //服务器端接收的是客户端StudentPojo对象, 需要进行解码
                           pipeline.addLast(new ProtobufDecoder(StudentPojo.Student.getDefaultInstance()));
                           //Google Protocol Buffers编码器
                           pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                           //Google Protocol Buffers编码器
                           pipeline.addLast(new ProtobufEncoder());
                           // 自定义handler
                           pipeline.addLast(new ProtoBufClientHandler());
                       }
                   });
   
           try {
               ChannelFuture future = bootstrap.connect("localhost", 4444).sync();
               future.channel().closeFuture().sync();
           } catch (InterruptedException e) {
               e.printStackTrace();
           } finally {
               worker.shutdownGracefully();
           }
       }
   }
   
   ```

   ```java
   // 客户端处理器
   public class ProtoBufClientHandler extends SimpleChannelInboundHandler<StudentPojo.Student> {
       @Override
       public void channelActive(ChannelHandlerContext ctx) throws Exception {
           StudentPojo.Student stu = StudentPojo.Student.newBuilder().setAge(10).setId(1).setName("student from client").build();
           ctx.channel().writeAndFlush(stu);
       }
   
       @Override
       protected void channelRead0(ChannelHandlerContext ctx, StudentPojo.Student msg) throws Exception {
           System.out.println("收到服务器的对象 " + msg);
       }
   
       @Override
       public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
           ctx.close();
       }
   }
   ```

4. 核心总结

   几个核心的protobuf处理器

   1.  ProtobufVarint32FrameDecoder: 这是针对protobuf协议的ProtobufVarint32LengthFieldPrepender()所加的长度属性的解码器 可选
   2. ProtobufDecoder 解码器 必选
   3. ProtobufVarint32LengthFieldPrepender: 对protobuf协议的的消息头上加上一个长度为32的整形字段，用于标志这个消息的长度 可选
   4. ProtobufEncoder: 编码器 必选

   ```java
                      // ProtobufVarint32FrameDecoder，通过Google Protocol Buffers序列化框架动态的切割接收到的ByteBuf
   
                           //服务器端接收的是客户端StudentPojo对象, 需要进行解码
                           pipeline.addLast(new ProtobufDecoder(StudentPojo.Student.getDefaultInstance()));
                           //Google Protocol Buffers编码器
                           pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                           //Google Protocol Buffers编码器
                           pipeline.addLast(new ProtobufEncoder());
   ```