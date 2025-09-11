package com.jxw.yuliao

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.multidex.MultiDex
import com.jxw.basemodel.BuildConfig
import com.jxw.basemodel.base.BaseApplication
import com.jxw.basemodel.constant.ConstantDataStore
import com.jxw.basemodel.network.HttpManager
import com.jxw.basemodel.utils.DataStoreUtils
import com.jxw.validate.utils.LauncherDataUtils
import com.jxw.yuliao.activity.SplashActivity
import com.jxw.yuliao.manager.AppExecutorsManager
import com.jxw.yuliao.manager.EmojiManager
import com.jxw.yuliao.utils.CrashHandlerManager
import com.jxw.yuliao.utils.DemoLog
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.interfaces.TUICallback
import com.tencent.qcloud.tuicore.interfaces.TUILoginListener
import com.tencent.qcloud.tuicore.util.ToastUtil
import com.tencent.qcloud.tuikit.timcommon.util.TUIUtil

/**
 *
 * @author: jyq
 * @date: 2023/11/9
 * @descp: applicaiton
 */
class App : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        if (isMainProcess()) {
            try {
                initSync()
                initDelay()
                initAsync()
            } catch (e: Exception) {
                Log.e("App", "init", e)
            }
        }
    }

    /**
     * 同步初始化
     */
    private fun initSync() {
        CrashHandlerManager.init(this)
        if (BuildConfig.DEVICE_TYPE == 0) {
            LauncherDataUtils.init(this, false)
        } else {
            LauncherDataUtils.init(this, true)
        }
        instance = this
        HttpManager.init(this)
        MultiDex.install(this)
        registerActivityLifecycleCallbacks(StatisticActivityLifecycleCallback())
        initLoginStatusListener()
        DataStoreUtils.putData(ConstantDataStore.APP_VERSION_CODE, com.jxw.yuliao.BuildConfig.VERSION_CODE)
        DataStoreUtils.putData(ConstantDataStore.APP_PACKAGE_NAME, com.jxw.yuliao.BuildConfig.APPLICATION_ID)
    }

    /**
     * 延迟初始化
     */
    private fun initDelay() {
        AppExecutorsManager.mainThread.executeDelay({

        }, 3000)
    }

    /**
     * 异步初始化
     */
    private fun initAsync() {
        AppExecutorsManager.cpuIO.execute {
//            EmojiManager.init(this)
        }
    }


    fun initLoginStatusListener() {
        TUILogin.addLoginListener(loginStatusListener)
    }

    private val loginStatusListener: TUILoginListener = object : TUILoginListener() {
        override fun onKickedOffline() {
            ToastUtil.toastLongMessage(getInstance().getString(R.string.repeat_login_tip))
            logout()
        }

        override fun onUserSigExpired() {
            ToastUtil.toastLongMessage(getInstance().getString(R.string.expired_login_tip))
            TUILogin.logout(object : TUICallback() {
                override fun onSuccess() {
                    logout()
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    logout()
                }
            })
        }
    }

    fun logout() {
        DemoLog.i(TAG, "logout")
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("LOGOUT", true)
        startActivity(intent)
        MainActivity.finishMainActivity()
    }

    internal class StatisticActivityLifecycleCallback : ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
            DemoLog.i(TAG, "onActivityCreated bundle: $bundle")
            if (bundle != null) {
                // restart app
                val intent = Intent(activity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                getInstance().startActivity(intent)
            }
        }

        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }

    private fun isMainProcess(): Boolean {
        val am = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val mainProcessName = this.packageName
        val currentProcessName: String = TUIUtil.getProcessName()
        return mainProcessName == currentProcessName
    }

    companion object {
        private lateinit var instance: App
        fun getInstance() = instance
        private const val TAG = "App"
    }
}