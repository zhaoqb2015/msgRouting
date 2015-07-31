package com.gvsoft.analyse;

import com.gvsoft.Config;
import com.gvsoft.analyse.inf.IHandle;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: zhaoqiubo
 * Date: 15/7/27
 * Time: 上午10:38
 * To change this template use File | Settings | File Templates.
 */
public class AnalyseTools {

    private static final String UTF_8 = "UTF-8";
    private static CharsetEncoder encoder = Charset.forName(UTF_8).newEncoder();
    private static CharsetDecoder decoder = Charset.forName(UTF_8).newDecoder();

    /**
     * 拆分客户端发送来的报文，分为header和body
     * @param str
     * @return
     */
    public static IHandle analyseHandle(String str) {

        int position = str.indexOf(Config.HBREGEX);
        if (position > 0) {
            AbstractHandle ahm = new AbstractHandle();
            String strSuffix = str.substring(position + 1);
            int posSuffix = strSuffix.indexOf(Config.HBREGEX);
            if (posSuffix > 0) {
                ahm.initHandle(str.substring(0, position), strSuffix.substring(0, posSuffix),strSuffix.substring(posSuffix+1));
                return ahm;
            }else{
                return null;
            }
        } else {
            return null;
        }

    }

    public static ByteBuffer encode(CharBuffer in) throws CharacterCodingException {
        return encoder.encode(in);
    }

    public static CharBuffer decode(ByteBuffer in) throws CharacterCodingException {
        return decoder.decode(in);
    }

    public static boolean isContain(String[] strArray, String str) {

        for (int i = 0; i < strArray.length; i++) {
            if (strArray[i].equals(str)) {
                return true;
            }

        }
        return false;
    }

    /*
    根据分隔符，将字符串分割为数组
     */
    public static String[] str2ArrayByChar(String string, String divisionChar) {
        int i = 0;
        StringTokenizer tokenizer = new StringTokenizer(string, divisionChar);

        String[] str = new String[tokenizer.countTokens()];

        while (tokenizer.hasMoreTokens()) {
            str[i] = new String();
            str[i] = tokenizer.nextToken();
            i++;
        }

        return str;
    }

    public static void main(String arg[]) throws Exception {

        String str = "L|42342341|{\"clientId\":\"0e73b8bf-a00b-f98c-f49c-4e1dc1c162d9\"}";
        AnalyseTools.analyseHandle(str);
//        String str = "R:com.gvsoft.analyse.LoginHandle|E:com.gvsoft.analyse.ErrorHandle";
//        String[] strArray = AnalyseTools.str2ArrayByChar(str, "|");
        System.out.println("S");


    }
}
