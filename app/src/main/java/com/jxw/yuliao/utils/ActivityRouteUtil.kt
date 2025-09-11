package com.jxw.yuliao.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import com.jxw.basemodel.BuildConfig


/**
 *
 * @author: jyq
 * @date: 2023/12/17
 * @descp: 路由跳转
 */
object ActivityRouteUtil {

    /**
     * 跳转到Wifi页面
     *
     * @param context
     */
    fun toWlanActivity(context: Context) {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        if (BuildConfig.DEVICE_TYPE == 0) {
            intent.setClassName("com.jxw.launcher", "com.jxw.launcher.SPWLANActivity")
        } else {
            intent.setClassName("com.jxw.launcher", "com.jxw.launcher.activity.WLANActivity")
        }
        try {
            context.startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            try {
                context.startActivity(intent)
            } catch (ne: java.lang.Exception) {
                ne.printStackTrace()
            }
        }
    }
}
