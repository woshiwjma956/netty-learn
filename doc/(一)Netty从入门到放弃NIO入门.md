## NIO三大组件

- ## Buffer 缓冲区

  Nio中的Buffer可以理解为一个数据容器，任何对数据的操作都是在Buffer中进行的。Nio中从Buffer中写入数据到Channel，或者从Channel中读取数据到Buffer。Buffer子类主要有基本类型的几种Buffer其中最常用的是ByteBuffer。

  ### 核心属性

  - capacity: Buffer 的缓冲区大小 一旦声明不可变
  - limit 界限 表示缓冲区操作数据大小 limit 之后的数据不可读写,可以修改
  - position 位置 指针 缓冲区下一个要被操作数据的位置或者指针
  - mark 表示 可以标记当前position的位置

  

  ### 常用API

  - allocate(size) 创建一个指定大小的Buffer
  - flip() 读写模式切换
  - get() 从Buffer中获取数据
  - put() 将输入存入Buffer中
  - clear() 清空Buffer 本质上是把position 和limit 重置
  - mark() 获取当前position
  - reset() 将当前的position 重置为mark
  - rewind() 重读操作 将position 重新置为0
  
  ### 测试代码
  
  ```java
  public class NioBufferDemo {
  
      public static void main(String[] args) {
          // 使用Buffer.allocate 创建一个指定大小的buffer
          IntBuffer intBuffer = IntBuffer.allocate(5);
  
          for (int i = 0; i < intBuffer.capacity(); i++) {
              int i1 = i * 2;
              System.out.println(String.format("put %d in buffer", i1));
              intBuffer.put(i1);
          }
  
          // buffer 读写模式切换 一定要记得翻转一下 将指针重新指向开头
          // 翻转的本质就是将limit = position 然后 position 置0  mark改为-1
          intBuffer.flip();
  		// 是否有剩余
          while (intBuffer.hasRemaining()) {
              System.out.println("take buffer => "+intBuffer.get());
          }
      }
  
  }
  ```

- ## Channel通道

  channel是NIO三大组件中的用于数据传输的组件。类似于传统BIO中的流，但是与BIO不同的是传统的BIO Inputstream Outputstream是单向的，NIO channel是双向的能读能写，实现异步读写数据，面向缓冲区Buffer。

  Channel是NIO中的一个接口

  ```java
  // java.nio.channels.Channel
  public interface Channel extends Closeabl
  ```

  - ### 常用主要实现类

    - FileChannel 文件读写操作
    - DatagramChannel UDP数据读写操作
    - ServerSocketChannel TCP读写操作类似传统的ServerSocket
    - SocketChannel TCP读写操作类似传统的Socket

  - #### FileChannel 核心方法

    ```java
    public int read(ByteBuffer dst)//从通道中读取数据到Buffer中
    public int write(ByteBuffer src)// 将Buffer中数据写入channel
    public long transferFrom(ReadableByteChannel src,
                             long position, 
                             long count) //从目标通道复制数据到当前通道
    public long transferTo(long position, 
                           long count,
                           WritableByteChannel target) // 将当前通道数据写入到目标通道
    ```

    

  - #### FileChannel代码演示

    ```java
    	/**
         * 数据读取    channel.read(buffer);
         * @throws IOException
         */
        private static void readFromChannel() throws IOException {
            File file = new File("1.txt");
            FileInputStream fileInputStream = new FileInputStream(file);
            // 通过inputStream.getChannel() 获取当前文件的FileChannel
            FileChannel channel = fileInputStream.getChannel();
            // 创建一个buffer 用于 读数据
            ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
            // 把Channel中的数据读到buffer中
            channel.read(buffer);
            // 打印
            System.out.println(new String(buffer.array()));
        }
    ```

    ```java
    	 /**
         * 数据写入  channel.write(byteBuffer);
         * @throws IOException
         */
        private static void writeToChannel() throws IOException {
            // 模拟内从
            String content = "hello world netty is good!";
            // 创建一个buffer 用于保存数据
            ByteBuffer byteBuffer = ByteBuffer.allocate(content.length());
            // 将数据写入到buffer中
            byteBuffer.put(content.getBytes());
            // 注意一定要flip!!!!读写切换 想指针重新指向开头 不然写入不到任何数据
            byteBuffer.flip();
            File file = new File("output.txt");
            // 创建文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            // 获取channel
            FileChannel channel = fileOutputStream.getChannel();
            // 将buffer中的数据写入到channel中
            channel.write(byteBuffer);
            // 关闭流自动关闭channel
            fileOutputStream.close();
        }
    ```

    ```java
     	/**
         * 文件拷贝 transferFrom
         * @throws IOException
         */
        private static void transferFromApi() throws IOException {
            File src =  new File("1.txt");
            FileInputStream inputStream = new FileInputStream(src);
            File dest =  new File("1_copy.txt");
            FileOutputStream outputStream = new FileOutputStream(dest);
    
            FileChannel inputChannel = inputStream.getChannel();
            FileChannel outputChannel = outputStream.getChannel();
            // channel实现文件拷贝
            outputChannel.transferFrom(inputChannel,0,src.length());
    
            inputStream.close();
            outputStream.close();
        }
    ```

    ```java
    // 基于NIO实现的文件拷贝方法
    /**
         * 通过NIO实现文件复制
         * @param src
         * @param dest
         * @throws IOException
         */
        private static void copyByNIO(String src, String dest) throws IOException {
            File file = new File(src);
            FileInputStream inputStream = new FileInputStream(file);
            FileChannel inputChannel = inputStream.getChannel();
    
            FileOutputStream outputStream = new FileOutputStream(new File(dest));
            FileChannel outputChannel = outputStream.getChannel();
            // 创建buffer 数据中间保存的容器
            ByteBuffer buffer = ByteBuffer.allocate(2);
    
            while (true) {
                // 清空buffer
                buffer.clear();
                int read = inputChannel.read(buffer);
                // 如果已经读完了
                if (read < 0) {
                    break;
                }
                // 翻转
                buffer.flip();
                // 写入
                outputChannel.write(buffer);
            }
    
            inputStream.close();
            outputStream.close();
        }
    ```

    