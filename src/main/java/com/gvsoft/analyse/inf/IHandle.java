package com.gvsoft.analyse.inf;

/**
 * Created with IntelliJ IDEA.
 * User: zhaoqiubo
 * Date: 15/7/27
 * Time: 上午10:54
 * To change this template use File | Settings | File Templates.
 * 处理模型接口
 */
public interface IHandle {


    public void initHandle(Object header,Object rid,Object body);

    //public void setHandleHeader(Object header);

    //public void setHandleBody(Object body);

    public Object getHandleHeader();

    public Object getHandleBody();

    public Object getRid();

    public String getPackets();

}
