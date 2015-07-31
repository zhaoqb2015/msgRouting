package com.gvsoft.analyse;

import com.alibaba.fastjson.JSON;

/**
 * 处理错误的模型
 * Created with IntelliJ IDEA.
 * User: zhaoqiubo
 * Date: 15/7/28
 * Time: 下午3:40
 * To change this template use File | Settings | File Templates.
 */
public class ErrorHandle extends AbstractHandle {

    private String errCode;
    private String errMsg;
    public final static String HEADER = "E";
    public final static String INVAILD_REQ_CODE = "10001";
    public final static String INVAILD_REQ_MSG = "无效请求！";

    public final static String FORCE_KICK_CODE = "10002";
    public final static String FORCE_KICK_MSG = "同一CLIENT_ID在其他设备登录，当前通道被强制关闭！";

    public final static String CLIENT_EXIST_CODE = "10003";
    public final static String CLIENT_EXIST_MSG = "当前CLIENT_ID已建立通道，如需登录请使用强制登录请求！";



    public void initHandle(String errCode,String errMsg){
        this.header = HEADER;
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.rid = this.getNewRid();
        StringBuffer sb = new StringBuffer();
        sb.append("{\"errCode\":").append(errCode).append(",\"errMsg\":").append(errMsg).append("}");
        this.body = sb.toString();
    }
    public String getErrCode() {
        return this.errCode;
    }
    public String getErrMsg(){
        return this.errMsg;
    }


}
