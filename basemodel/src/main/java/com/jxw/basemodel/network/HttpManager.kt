package com.jxw.basemodel.network

import android.content.Context
import android.util.Log
import com.jxw.basemodel.base.IBaseManager
import com.jxw.basemodel.utils.NetworkStatusUtil
import com.jxw.validate.utils.LauncherDataUtils
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.cache.CacheMode
import java.io.File
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

/**
 *
 * @author: jyq
 * @date: 2023/12/16
 * @descp: 网络请求manager
 */
object HttpManager : IBaseManager {

    private const val cacheSize = 10 * 1024 * 1024 // 10 MiB

    override fun init(context: Context) {
        UrlManager.init(context)
        TrustManager.init(context)
        RxHttpPlugins.init(getHttpClient(context))      //自定义OkHttpClient对象
            .setDebug(true, true, 2)      //是否开启调试模式、是否分段打印、打印json数据缩进空格数量
            .setCache(
                getHttpCacheFile(context),
                cacheSize.toLong(),
                CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE
            )
            .setOnParamAssembly { it.addHeader("token", LauncherDataUtils.token) }
    /*        .setOnParamAssembly {
//                it.addHeader("token", "login_jxwdevice20002787dguk1zfyrnfnpgqc5n9s8ataruu2ue8n")
                it.addHeader("token", "login_jxwdevice20002787kh0p6gw3ldpshu1jg9a4297zkl3jksux")
            }*/


    }

    /**
     * 获取 okhttp缓存文件
     */
    private fun getHttpCacheFile(context: Context): File =
        File(context.applicationContext.cacheDir, context.applicationContext.packageName)

    /**
     * 获取 okhttpClient
     */
    private fun getHttpClient(context: Context): OkHttpClient {
        val builder = OkHttpClient.Builder().apply {
            addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    if (NetworkStatusUtil.isConnected(context)) {
                        val request = chain.request()
                        return chain.proceed(request)
                    } else {
                        throw NoNetWorkException()
                    }
                }
            })
        }
        builder.connectTimeout(15, TimeUnit.SECONDS)
        builder.readTimeout(15, TimeUnit.SECONDS)
        builder.writeTimeout(15, TimeUnit.SECONDS)
        builder.sslSocketFactory(
            TrustManager.getSslContextFactory(),
            TrustManager.getWeakTrustManager()
        )
        builder.hostnameVerifier(HostnameVerifier { hostname, session ->
            Log.e("HttpManager", "hostname:$hostname")
            //先判断证书是否是受信任的 弱校验
            val hv = HttpsURLConnection.getDefaultHostnameVerifier()
            if (hv.verify(hostname, session)) {
                //判断域名是否包含
                return@HostnameVerifier isContain(hostname)
            }
            false
        })
        return builder.build()


    }

    //域名白名单
    private val whiteList = arrayListOf(
        "ksyuncs.com",
        "jxwxxkj.com",
        "zhinengtongbu.com",
        "jiumentongbu.com",
        "txzyb.com",
        "xuexizhiwang.com"
    )

    private fun isContain(hostName: String): Boolean {
        for (i in whiteList.indices) {
            if (hostName.contains(whiteList[i])) {
                return true
            }
        }
        return false
    }

    override fun clear(context: Context?) {

    }
}