package com.gvsoft;

import com.gvsoft.analyse.*;
import com.gvsoft.analyse.inf.IHandle;
import net.ymate.platform.commons.logger.Logs;

import javax.print.DocFlavor;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: zhaoqiubo
 * Date: 15/7/21
 * Time: 下午4:05
 * To change this template use File | Settings | File Templates.
 */
public class GVServer implements Runnable {

    //事件选择器
    private static Selector selector;
    //缓冲区大小设定
    private static final int BLOCK = 1024;
    //存储所有活跃连接
    public static Map ACTIVED_CHANNELS = new ConcurrentHashMap();
    //所支持的指令集
    private static final String SupportHandleCodeArray[] = {"R"};

    private static final int PACKET_HEAD_LENGTH = 4;

    public static void main(String arg[]) throws Exception {
        //初始化配置数据
        Config cfg = new Config(arg[0]);
        final GVServer gvServer = new GVServer();
        //启动ServerSocket通道
        gvServer.initServer(cfg);
        //开始接收客户端事件key
        (new Thread(gvServer)).run();

    }

    private void initServer(Config cfg) throws IOException {
        try {
            //打开一个serversocket通道
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            //将这个serversokect通道设置为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            //绑定serversokect的ip和端口
            serverSocketChannel.socket().bind(new InetSocketAddress(cfg.getIp(), cfg.getPort()));
            //打开选择器
            selector = Selector.open();
            //将此通道注册给选择器selector
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("GVServer服务已经启动，开始接收客户端连接…………");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        while (true) {
            try {

                //监听事件key
                selector.select();
                System.out.println("监听到一组key，看看都有啥……");
                //迭代一组事件key
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    //定义一个socket通道
                    SocketChannel socketChannel = null;

                    int count = 0;

                    SelectionKey key = keys.next();
                    System.out.println("感兴趣的：" + key.interestOps());
                    //删除Iterator中的当前key，避免重复处理
                    keys.remove();
                    if (!key.isValid()) {
                        continue;
                    } else if (key.isAcceptable()) {
                        //从客户端送来的key中获取ServerSocket通道
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        //接收此ServerSocket通道中的Socket通道
                        socketChannel = serverSocketChannel.accept();
                        //将此socket通道设置为非阻塞模式
                        socketChannel.configureBlocking(false);
                        //将此通道注册到selector，并等待接收客户端的读入数据
                        socketChannel.register(selector, SelectionKey.OP_READ);

                    } else if (key.isReadable()) {


                        //获取事件key中的channel
                        socketChannel = (SocketChannel) key.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(BLOCK);
                        //清理缓冲区，便于使用
                        byteBuffer.clear();
                        //将channel中的字节流读入缓冲区
                        String readStr = "";
                        count = socketChannel.read(byteBuffer);
                        if (count > 0) {
                            readStr = new String(byteBuffer.array(), 0, count);
                            handleRead(readStr, key);
                        } else {
                            socketChannel.close();
                            socketChannel = null;
                        }

                    } else if (key.isWritable()) {
                        ((SocketChannel) key.channel()).register(selector, SelectionKey.OP_READ);
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 处理
     *
     * @param readStr
     * @param key
     */
    private void handleRead(String readStr, SelectionKey key) {

        IHandle handleModel = AnalyseTools.analyseHandle(readStr);
        if (handleModel == null || Config.HANDLE_MODEL_MAP.get(handleModel.getHandleHeader()) == null) {
            //不在服务端识别范围内的报文，回复E响应
            ErrorHandle errorHandle = (ErrorHandle) Config.HANDLE_MODEL_MAP.get(ErrorHandle.HEADER);
            errorHandle.initHandle(ErrorHandle.INVAILD_REQ_CODE, ErrorHandle.INVAILD_REQ_MSG);
            SocketChannel socketChannel = (SocketChannel) key.channel();
            Logs.info("客户端写入非法报文："+readStr);
            write2Client(errorHandle, socketChannel);

        } else {

            //发送针对客户端的R响应
            ReplyHandle replyHandle = (ReplyHandle) Config.HANDLE_MODEL_MAP.get(ReplyHandle.HEADER);
            replyHandle.initHandle(handleModel.getRid());
            write2Client(replyHandle, (SocketChannel) key.channel());
            //针对注册请求的处理
            if (handleModel.getHandleHeader().equals(LoginHandle.HEADER)) {

                //实例化注册请求模型
                LoginHandle loginHandle = (LoginHandle) AbstractHandle.getInstance(LoginHandle.HEADER);

                //初始化注册请求模型
                loginHandle.initHandle(handleModel.getHandleBody());
                loginHandle.handlePacket((SocketChannel)key.channel());
                //将注册后的channel保存到全局map中
                //GVServer.ACTIVED_CHANNELS.put(loginHandle.getClientId(), (SocketChannel) key.channel());
                Logs.info("新注册的通道已经加入通道缓冲区，缓冲区的size为" + GVServer.ACTIVED_CHANNELS.size());
//                write2Client("服务端已经获取客户端身份！" + loginHandle.getClientId(), (SocketChannel) key.channel());
            }
        }
    }

    /**
     * 向客户端写入信息的方法
     * @param iHandle 报文处理类接口
     * @param socketChannel
     */
    public static void write2Client(IHandle iHandle, SocketChannel socketChannel) {
        try {
            socketChannel.register(selector, SelectionKey.OP_WRITE);
            //创建一个byteBuffer用来存储要写入的buffer
            ByteBuffer byteBuffer = ByteBuffer.allocate(BLOCK);
            //得出整个包体的长度
            String packetSize = Integer.toString(iHandle.getPackets().getBytes().length);
            //讲包体长度放入buffer的前四位
            byteBuffer.put(packetSize.getBytes());
            //移动buffer的postion指针到第四位，包体将从第四位开始写入
            byteBuffer.position(PACKET_HEAD_LENGTH);
            //写入包体
            byteBuffer.put(iHandle.getPackets().getBytes());

            byteBuffer.flip();
            socketChannel.write(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
