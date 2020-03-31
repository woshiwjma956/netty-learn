package com.ycorn.netty.dubborpc.costumer;

import com.ycorn.netty.dubborpc.netty.DubboNettyClient;
import com.ycorn.netty.dubborpc.publicinterface.HelloService;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-11 18:35
 */
public class Customer {
    //这里定义协议头
    public static final String providerName = "dubbo#HelloService#hello#";    //这里定义协议头

    public static void main(String[] args) {
        DubboNettyClient client = new DubboNettyClient();
        HelloService helloService = (HelloService) client.getBean(HelloService.class, providerName);
        String result = helloService.hello("duboo");
        System.out.println(result);
    }
}