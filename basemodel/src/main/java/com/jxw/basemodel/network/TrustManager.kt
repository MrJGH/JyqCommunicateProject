package com.jxw.basemodel.network

import android.content.Context
import com.jxw.basemodel.base.IBaseManager
import java.security.KeyStore
import java.security.KeyStoreException
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 *
 * @author: jyq
 * @date: 2023/12/16
 * @descp: 证书校验manager
 */
object TrustManager : IBaseManager {
    private lateinit var mWeakTrustManager: X509TrustManager
    private lateinit var mSslContext: SSLContext

    override fun init(context: Context) {
        try {
            // 创建使用系统认证证书的TrustManager
            val trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val systemTrustManager: X509TrustManager = findSystemTrustManager(trustManagerFactory)

            // 创建使用弱SSL校验的TrustManager
            mWeakTrustManager = WeakX509TrustManager(systemTrustManager)

            // 创建SSL上下文并配置TrustManager
            mSslContext = SSLContext.getInstance("TLS")
            mSslContext.init(null, arrayOf(mWeakTrustManager), null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @Throws(KeyStoreException::class)
    private fun findSystemTrustManager(trustManagerFactory: TrustManagerFactory): X509TrustManager {
        for (trustManager in trustManagerFactory.trustManagers) {
            if (trustManager is X509TrustManager) {
                return trustManager
            }
        }
        throw IllegalStateException("Unable to find system TrustManager")
    }

    fun getWeakTrustManager() = mWeakTrustManager

    fun getSslContextFactory() = mSslContext.socketFactory


    override fun clear(context: Context?) {

    }
}