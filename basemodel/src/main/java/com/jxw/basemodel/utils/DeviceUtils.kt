package com.jxw.basemodel.utils

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.TelephonyManager

/**
 * 类描述:
 */
object DeviceUtils {
    /**
     * 获取设备IMEI
     *
     * @param context 上下文
     * @return
     */
    @SuppressLint("MissingPermission")
    fun getIMEI(context: Context): String? {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.deviceId
        } catch (e: Exception) {
            return ""
        }
    }
}