package com.tencent.qcloud.tuikit.tuichat.classicui.media;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Common {

    /**
     * 配置信息，常量
     */
    public static class Constance{

        public static Uri USERURI = Uri.parse("content://com.jxw.wechat.provider/User");
        public static String API_URL = "http://api4.jiumentongbu.com/";
    //    String API_URL = "http://pre.test.jiumentongbu.com/"; //半正式环境
    //    String API_URL = "http://api3.test.jiumentongbu.com/"; //测式环境

        static {
            setPlatFormType();
        }

        /**
         * 语音音频文件保存位置
         */
        public static   String DIR_AUDIO;
        /**
         * 最大聊天信息数量
         */
        public static final int LIMIT_MESSAGE = 50;
        /**
         * SP 名称
         */
        public static final String PREFERENCE = "JXWINFO";
        //mqtt
        public static final String MQTT_USER_NAME = "username";
        public static final String MQTT_URL = "mqturl";
        public static final String MQTT_CLIENT_ID = "clientId";
        public static final String MQTT_PASSWORD = "mqtpasswd";


        public static void setPlatFormType(){
            File file = new File("/sdcard/config.ini");
            try{
                FileInputStream inputStream = new FileInputStream(file);
                setPlatform(readFileContent(inputStream));
                //Log.i("zzj","zzj the PlatFormType="+PLATFORM_VERSION);

            }catch (IOException e){
                setPlatform(null);
                e.printStackTrace();
            }
        }
        private static void setPlatform(String type){
            Log.i("zzj","zzj the PlatFormType="+type);
            if ("test".equals(type)) {
                API_URL = "http://api3.test.jiumentongbu.com/";
            } else if ("gray".equals(type)) {
                API_URL = "http://pre.test.jiumentongbu.com/";
            } else {
                // release
                API_URL = "http://api4.jiumentongbu.com/";

            }
        }
        private static String readFileContent(InputStream inputStream) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = null;
            StringBuffer sbf = new StringBuffer();
            try {
                reader = new BufferedReader(inputStreamReader);
                String tempStr;
                while ((tempStr = reader.readLine()) != null) {
                    sbf.append(tempStr);
                }
                reader.close();
                return sbf.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            return sbf.toString();
        }
    }



}
