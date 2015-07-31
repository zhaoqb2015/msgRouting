package com.gvsoft.analyse;

import com.alibaba.fastjson.JSON;
import com.gvsoft.Config;
import com.gvsoft.GVServer;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * 处理客户端身份标识的模型，每个客户端需要有唯一的身份标识。
 * Created with IntelliJ IDEA.
 * User: zhaoqiubo
 * Date: 15/7/27
 * Time: 下午2:40
 * To change this template use File | Settings | File Templates.
 */
public class LoginHandle extends AbstractHandle {

    public final static String HEADER = "L";

    public final static String FORCE = "0";

    public final static String NOT_FORCE = "1";

    public String clientId;
    public String isForce;

    public void initHandle( Object body){

        try {
            this.clientId = JSON.parseObject((String)body).get("clientId").toString();
            this.isForce = JSON.parseObject((String)body).get("isForce").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

    };

    public String getClientId() {
        return clientId;
    }
    public String getIsForce(){return this.isForce;}

    public void handlePacket(SocketChannel socketChannel){

        SocketChannel scPreChannel = (SocketChannel)GVServer.ACTIVED_CHANNELS.get(this.getClientId());

        try {
            //如果当前激活通道的map中已经存在该clientid的通道，并且为强制登录。
            // 则说明当前请求的client需要把原来clientid的通道强制踢掉
            if (scPreChannel!=null && this.getIsForce()==FORCE){
                //通知之前的具有相同的clientid的客户端，他已经被强制下线。
                ErrorHandle errorHandle = (ErrorHandle)Config.HANDLE_MODEL_MAP.get(ErrorHandle.HEADER);
                errorHandle.initHandle(ErrorHandle.FORCE_KICK_CODE, ErrorHandle.FORCE_KICK_MSG);
                GVServer.write2Client(errorHandle,scPreChannel);
                //关闭之前的通道
                scPreChannel.close();
                scPreChannel =null;
                GVServer.ACTIVED_CHANNELS.remove(this.clientId);

                //如果当前激活通道内已经存在该clientid的通道，那么直接返回给
                // 当前通道的申请者，让其选择放弃登录或者强制登录
            }else if(scPreChannel!=null) {
                ErrorHandle errorHandle = (ErrorHandle) Config.HANDLE_MODEL_MAP.get(ErrorHandle.HEADER);
                errorHandle.initHandle(ErrorHandle.CLIENT_EXIST_CODE, ErrorHandle.CLIENT_EXIST_MSG);
                GVServer.write2Client(errorHandle, socketChannel);
                //如果以上都不是，直接放入活动通道Map中，登录成功
            }else{
                //将注册后的channel保存到全局map中
                GVServer.ACTIVED_CHANNELS.put(this.getClientId(), socketChannel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
