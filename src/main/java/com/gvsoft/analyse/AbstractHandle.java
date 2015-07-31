package com.gvsoft.analyse;

import com.gvsoft.Config;
import com.gvsoft.analyse.inf.IHandle;

/**
 * Created with IntelliJ IDEA.
 * User: zhaoqiubo
 * Date: 15/7/27
 * Time: 下午2:53
 * To change this template use File | Settings | File Templates.
 */
public class AbstractHandle implements IHandle {



    Object header;
    Object body;
    Object rid;

    private static int newRid = 0;

    public static Object getInstance(String classKey) {
        return Config.HANDLE_MODEL_MAP.get(classKey);
    }

    public void initHandle(Object header,Object rid,Object body) {
        this.header = header;
        this.body = body;
        this.rid = rid;
    }

    public Object getHandleHeader() {
        return this.header;
    }

    public Object getHandleBody() {
        return this.body;
    }

    public Object getRid() {
        return this.rid;
    }

    /**
     * 返回报文序列号，该序列号仅作为响应的对应依据，不唯一，不可作为业务标记。
     * @return
     */
    public static int getNewRid(){
        if (newRid >1000000000){
            newRid = 0;
        }
        return newRid++;
    }

    public String getPackets(){
        StringBuffer sb = new StringBuffer();
        sb.append(this.getHandleHeader()).append("|").append(this.getRid()).append("|").append(this.getHandleBody());
        return sb.toString();
    }

}
