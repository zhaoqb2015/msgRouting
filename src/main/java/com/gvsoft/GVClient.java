package com.gvsoft;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: zhaoqiubo
 * Date: 15/7/27
 * Time: 下午3:55
 * To change this template use File | Settings | File Templates.
 */
public class GVClient implements Runnable {

    private SocketChannel socketChannel = null;
    private final static int BLOCK = 1024;
    private Selector selector = null;
    private String userId = null;
    private static final int PACKET_HEAD_LENGTH = 4;

    /**
     * 登录报文头
     */
    private final static String LHEADER = "L";
    /**
     * 错误报文头
     */
    private final static String EHEADER = "E";
    /**
     * 消息报文头
     */
    private final static String MHEADER = "M";
    /**
     * 维持链路报文头
     */
    private final static String KHEADER = "K";


    public void connectServer(String ip, int port, String userId) {
        InetSocketAddress serverAddress = new InetSocketAddress(ip, port);
        try {
            this.userId = userId;
            //打开socket通道
            socketChannel = SocketChannel.open();
            //讲通道设置为非阻塞模式
            socketChannel.configureBlocking(false);
            //打开多路复用器
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            //连接服务器，会触发服务端selector.select()，key的状态为OP_ACCEPT
            socketChannel.connect(serverAddress);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run() {
        try {
            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                for (int i = 0; keys.hasNext(); i++) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (key.isConnectable()) {
                        socketChannel = (SocketChannel) key.channel();
                        if (socketChannel.isConnectionPending()) {
                            socketChannel.finishConnect();
                            System.out.println("成功连接服务端！");
                            //连接成功后，发送身份标识
                            String writeStr = LHEADER + "|111|{\"clientId\":\"" + userId + "\"}";
                            ByteBuffer byteBuffer = getByteBuffer(writeStr);
                            System.out.println("发送给服务端消息：" + writeStr);
                            socketChannel.write(byteBuffer);
                        }
                        socketChannel.register(selector, SelectionKey.OP_READ);

                    } else if (key.isReadable()) {
                        //获取事件key中的channel
                        socketChannel = (SocketChannel) key.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(BLOCK);
                        //清理缓冲区，便于使用
                        byteBuffer.clear();
                        //将channel中的字节流读入缓冲区
                        String readStr = "";
                        int count = socketChannel.read(byteBuffer);
                        //务必要把buffer的position重置为0
                        byteBuffer.flip();
                        //标记读取缓冲区起始位置
                        int location = 0;
                        if (count > 0) {
                            //如果缓冲区从0到limit的数量大于包体大小标记数字
                            while (byteBuffer.remaining() > PACKET_HEAD_LENGTH) {

                                String strBsize;//包体大小标记
                                strBsize = new String(byteBuffer.array(), location, PACKET_HEAD_LENGTH);
                                byteBuffer.position(location + PACKET_HEAD_LENGTH);//移动缓冲区position
                                System.out.println("包体大小：" + strBsize + "，查看position变化：" + byteBuffer.position());

                                int byteBufferSize = Integer.parseInt(strBsize.trim());//得到包体大小

                                if (byteBuffer.remaining() >= byteBufferSize) {//如果从缓冲区当前位置到limit大于包体大小，进行包体处理

                                    String strPacket = new String(byteBuffer.array(), PACKET_HEAD_LENGTH + location, byteBufferSize);
                                    byteBuffer.position(location + PACKET_HEAD_LENGTH + byteBufferSize);//将缓冲区的位置移动到下一个包体大小标记位置
                                    System.out.println("包体内容：" + strPacket + "，查看position变化：" + byteBuffer.position());
                                }
                                location = location + PACKET_HEAD_LENGTH + byteBufferSize;//设定读取缓冲区起始位置

                            }
                        } else {
                            socketChannel.close();
                            socketChannel = null;

                        }

                    } else if (key.isWritable()) {
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ByteBuffer getByteBuffer(String writeStr) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(BLOCK);
        byteBuffer.clear();
        byteBuffer.put(writeStr.getBytes());
        byteBuffer.flip();
        return byteBuffer;
    }

    public static void main(String arg[]) throws Exception {

        GVClient gvClient = new GVClient();
        gvClient.connectServer("127.0.0.1", 9001, "1");
        (new Thread(gvClient)).run();

//
//        byte[] a = {47,53};
//        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
//        String str = "test";
//        byteBuffer = byteBuffer.wrap(str.getBytes());
//        byteBuffer.put(a, 0, 2);
//        ByteBuffer byteBuffer1 = ByteBuffer.allocate(128);
//        byteBuffer1.put(a);
//        byteBuffer1.put(str.getBytes());
//        System.out.println();
    }

}
