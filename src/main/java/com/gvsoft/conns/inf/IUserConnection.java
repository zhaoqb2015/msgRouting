package com.gvsoft.conns.inf;

import java.nio.channels.SocketChannel;

/**
 * Created with IntelliJ IDEA.
 * User: zhaoqiubo
 * Date: 15/7/27
 * Time: 下午12:33
 * To change this template use File | Settings | File Templates.
 */
public interface IUserConnection {
    public void setUserId(String userid);
    public void setChannel(SocketChannel channel);
}
