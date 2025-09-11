package com.jxw.yuliao.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Enumeration;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * 类描述:
 */
public class DeviceUtils {

    private static TelephonyManager telephonyManager;

    /**
     * 国际移动用户识别码
     */
    public static TelephonyManager SIMCardInfo(Context context) {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager;
    }

    /**
     * 获取设备IMEI
     *
     * @param context 上下文
     * @return
     */
    public static String getIMEI(Context context) {
        try {
            telephonyManager = SIMCardInfo(context);
            if (telephonyManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (telephonyManager.getImei() == null || telephonyManager.getImei().isEmpty()) {
                        return telephonyManager.getDeviceId();
                    }
                    return telephonyManager.getImei();
                } else {
                    return telephonyManager.getDeviceId();
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取设备本机号码
     *
     * @param context 上下文
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getMobilePhone(Context context) {
        telephonyManager = SIMCardInfo(context);
        String nativePhoneNumber = telephonyManager.getLine1Number();
        return nativePhoneNumber;
    }


}
