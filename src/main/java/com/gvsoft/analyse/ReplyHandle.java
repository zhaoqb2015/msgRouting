package com.gvsoft.analyse;

/**
 * Created with IntelliJ IDEA.
 * User: zhaoqiubo
 * Date: 15/7/29
 * Time: 上午9:52
 * To change this template use File | Settings | File Templates.
 */
public class ReplyHandle extends AbstractHandle {
    public static final String HEADER ="R";
    public void initHandle(Object rid) {
        this.header = HEADER;
        this.rid = rid;
    }
    @Override
    public String getPackets(){
        StringBuffer sb = new StringBuffer();
        sb.append(this.getHandleHeader()).append("|").append(this.getRid());
        return sb.toString();
    }
}
