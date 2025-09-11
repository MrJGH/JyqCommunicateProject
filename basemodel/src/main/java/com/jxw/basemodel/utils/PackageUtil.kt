package com.jxw.basemodel.utils

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.jxw.basemodel.base.BaseApplication

/**
 *
 * @author: jyq
 * @desc:
 * @date: 2025/1/15
 */
object PackageUtil {
    /**
     * 获取指定报名的 版本号和版本名字
     */
    fun getAppVersion(packageName: String): Pair<Int, String>? {
        return try {
            val packageManager: PackageManager = BaseApplication.getInstance().packageManager
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            Pair(packageInfo.versionCode, packageInfo.versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            null // 如果找不到指定的包名，返回null
        }
    }
}