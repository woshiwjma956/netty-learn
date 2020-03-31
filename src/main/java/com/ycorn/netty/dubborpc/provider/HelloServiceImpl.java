package com.ycorn.netty.dubborpc.provider;

import com.ycorn.netty.dubborpc.publicinterface.HelloService;

/**
 * 描述:
 *
 * @author JimWu
 * @create 2020-03-11 18:11
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String msg) {
        return "你好 " + msg + " !";
    }
}