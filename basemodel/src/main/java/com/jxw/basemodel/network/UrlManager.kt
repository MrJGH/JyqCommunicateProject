package com.jxw.basemodel.network

import android.content.Context
import android.util.Log
import com.jxw.basemodel.base.IBaseManager
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 *
 * @author: jyq
 * @date: 2023/12/16
 * @descp: BaseUrl 管理工具类
 */
object UrlManager : IBaseManager {

    private var PLATFORM_VERSION = "" // 测试:test 灰度:gray 正式:release
    private var NEW_URL_HEADER: String = " https://gw-test.xuexizhiwang.com"
    private var OLD_MALL_URL_HEADER: String = "https://api3-test.jiumentongbu.com"
    private var permissionDenied = false
    private var MINI_PROGRAM_VERSION = 0 //小程序 版本 "正式版", 1  "开发版", 2 "体验版"

    override fun init(context: Context) {
        setPlatFormType()
    }

    override fun clear(context: Context?) {

    }

    fun getBaseOldDomain(apiPath: String): String {
        return OLD_MALL_URL_HEADER + apiPath
    }

    fun getNewBaseDomain(apiPath: String): String {
        return NEW_URL_HEADER + apiPath
    }

    /**
     * 获取小程序 版本 //0 "正式版", 1  "开发版", 2 "体验版"
     */
    fun getMiniProgramVersion() = MINI_PROGRAM_VERSION

    private fun setPlatFormType() {
        val file = File("/sdcard/config.ini")
        try {
            if (file.exists()) {
                PLATFORM_VERSION = FileInputStream(file).readFileContent()
                Log.i("UrlManager", "the PLATFORM_VERSION= $PLATFORM_VERSION")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            if ((e.message + "").contains("Permission denied")) {
                permissionDenied = true
            }
        } finally {
            setPlatform()
        }
    }

    private fun setPlatform() {
        when (PLATFORM_VERSION) {
            "test" -> {
//                OLD_MALL_URL_HEADER = "https://api3.test.jiumentongbu.com"
                OLD_MALL_URL_HEADER = "https://api3-test.xuexizhiwang.com"
                NEW_URL_HEADER = "https://gw-test.xuexizhiwang.com"
                MINI_PROGRAM_VERSION = 2
            }

            "gray" -> {
//                OLD_MALL_URL_HEADER = "https://gray.jiumentongbu.com"
                OLD_MALL_URL_HEADER = "https://gray.xuexizhiwang.com"
                NEW_URL_HEADER = "https://gw-pre.xuexizhiwang.com"
                MINI_PROGRAM_VERSION = 2
            }

            else -> {
                // release
//                OLD_MALL_URL_HEADER = "https://api4.jiumentongbu.com"
                OLD_MALL_URL_HEADER = "https://reg3.xuexizhiwang.com"
                NEW_URL_HEADER = "https://gwapi.xuexizhiwang.com"
                MINI_PROGRAM_VERSION = 0
            }
        }
    }

    private fun InputStream.readFileContent(): String {
        return use { it.bufferedReader().readText() }

    }
}