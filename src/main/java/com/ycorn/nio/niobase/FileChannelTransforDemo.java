package com.ycorn.nio.niobase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-01 20:39
 */
public class FileChannelTransforDemo {

    public static void main(String[] args) throws Exception {

        File file = new File("1.txt");
        FileInputStream inputStream = new FileInputStream(file);

        FileOutputStream outputStream = new FileOutputStream("2.txt");

        //inputStream.getChannel().transferTo(0,file.length(),outputStream.getChannel());
        outputStream.getChannel().transferFrom(inputStream.getChannel(),0,file.length());
        inputStream.close();
        outputStream.close();

    }

}