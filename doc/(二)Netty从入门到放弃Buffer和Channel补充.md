### 关于NIO Buffer和channel的补充功能

#### MappedByteBuffer

- 可以让文件直接在内存中（堆外内存）进行修改，减少一次操作系统的拷贝，当文件较大时，采用MappedByteBuffer，读写效率更高。

  传统的基于文件流的方式读取文件方式是系统指令调用，文件数据首先会被读取到进程的内核空间的缓冲区，而后复制到进程的用户空间，这个过程中存在两次数据拷贝；而内存映射方式读取文件的方式，也是系统指令调用，在产生缺页中断后，CPU直接从磁盘文件load数据到进程的用户空间，只有一次数据拷贝。

  ```java
  public class MappedByteBufferApiTest {
      public static void main(String[] args) throws Exception {
          /**
           * 参数说明:
           * 1. 文件路径
           * 2. 读写模式 one of {@code "r"}只读, {@code "rw"}读写, {@code "rws"}读写操作同步刷新到磁盘,刷新内容和元数据, {@code "rwd"}读写同步磁盘只刷新内容
           */
          RandomAccessFile file = new RandomAccessFile("1.txt", "rw");
          /**
           * 参数说明；
           * 1.FileChannel.MapMode.READ_WRITE 使用读写模式
           * 2.直接修改的起始位置
           * 3.从起始位置映射到内存的大小（不是索引），超过字节大小将不能修改
           */
          MappedByteBuffer mappedByteBuffer = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, file.length());
          mappedByteBuffer.put(0, (byte) 'x');
          byte[] bytes = new byte[(int) file.length()];
          mappedByteBuffer.get(bytes);
          System.out.println(new String(bytes));
      }
  }
  ```

#### Scattering Gathering

- NIO中可以通过对Buffer数组进行读写操作提供了Scattering Gathering API

  ```java
  public class ScatteringAndGatheringBufferApiTest {
  
      public static void main(String[] args) throws Exception {
          // buffer 数组
          ByteBuffer[] buffers = new ByteBuffer[2];
          ByteBuffer buffer1 = ByteBuffer.allocate(3);
          ByteBuffer buffer2 = ByteBuffer.allocate(5);
          buffers[0] = buffer1;
          buffers[1] = buffer2;
  
          ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
          InetSocketAddress inetSocketAddress = new InetSocketAddress(7777);
          // 绑定端口
          serverSocketChannel.socket().bind(inetSocketAddress);
          // 等待连接
          SocketChannel socketChannel = serverSocketChannel.accept();
          int messageLimit = buffer1.limit() + buffer2.limit();
          while (true) {
              long byteRead = 0;
              while (byteRead < messageLimit) {
                  // 读到buffer数组中
                  long read = socketChannel.read(buffers);
                  byteRead += read;
                  System.out.println("byteRead = " + byteRead);
                  Arrays.stream(buffers).map(t -> "position:=>" + t.position() + " limit=>" + t.limit()).forEach(System.out::println);
              }
  
              Arrays.asList(buffers).forEach(ByteBuffer::flip);
  
              long bytesWrite = 0;
  
              while (bytesWrite < messageLimit) {
                  long write = socketChannel.write(buffers);
                  bytesWrite += write;
              }
              Arrays.stream(buffers).forEach(ByteBuffer::clear);
              System.out.println("byteRead = " + byteRead + ",byteWrite = " + bytesWrite + ",messageLimit = " + messageLimit);
          }
  
      }
  }
  ```

  ```bat
  telnet localhost 7777
  crtl+] 进入命令模式
  ```

  

