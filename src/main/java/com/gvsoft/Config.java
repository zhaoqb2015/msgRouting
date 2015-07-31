package com.gvsoft;

import com.gvsoft.analyse.AnalyseTools;
import net.ymate.platform.base.YMP;
import net.ymate.platform.commons.logger.Logs;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


public class Config {

    //服务绑定的端口
    private int port;
    //服务器IP地址
    private String ip;
    /**
     * 报文区间分隔符，报文分为三个区间，HEADER区间、RID区间、BODY区间；
     * HEADER标记报文分类，RID为报文对象id，在一定周期内唯一；BODY为报文的内容区域。
     */
    public static String HBREGEX = "|";
    /**
     * 存储所有报文处理类的实例，一次性初始化进入Map
     */
    public static Map<String, Object> HANDLE_MODEL_MAP = new ConcurrentHashMap();
    /**
     * 配置报文处理类分类的外部分隔符，分隔符两边代表某一类报文处理类的属性描述。
     * 例如：R:com.gvsoft.analyse.RegisterHandleModel|E:com.gvsoft.analyse.ErrorHandleModel
     */
    private final String SeparatorOuter = "|";
    /**
     * 配置报文处理类分类的内部部分隔符,分割某一类报文处理类的header和body。
     * 例如：R:com.gvsoft.analyse.RegisterHandleModel
     */
    private final String SeparatorInner = ":";

    public static String SUPP_HANDLE_ARRAY[];

    public Config(String config) {
        initConfig(config);
    }

    /**
     *
     * @param config
     */
    private void initConfig(String config) {

        YMP.initialize();
        Properties p = new Properties();
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream
                    (System.getProperty("user.dir") + "/" + config), "utf-8");
            p.load(reader);
            this.setIp(p.getProperty("server_ip"));
            this.setPort(Integer.parseInt(p.getProperty("server_port").trim()));
            Config.HBREGEX=p.getProperty("regex");
            initHandleMap(p.getProperty("handle_class"));
            Logs.info("绿谷消息路由服务初始化完成！特别鸣谢：有理想的鱼亚美碟框架支持！");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void initHandleMap(String handleCfgStr) {
        String[] handleOuterArray = AnalyseTools.str2ArrayByChar(handleCfgStr, SeparatorOuter);
        try {
            for (int i = 0; i < handleOuterArray.length; i++) {
                String[] handleInnerArray = AnalyseTools.str2ArrayByChar(handleOuterArray[i], SeparatorInner);
                Class c = Class.forName(handleInnerArray[1]);
                Object handleClass = c.newInstance();
                Config.HANDLE_MODEL_MAP.put(handleInnerArray[0], handleClass);
                Logs.info("消息处理类"+handleClass+"加载完毕…………");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void initLog() {

    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


}
