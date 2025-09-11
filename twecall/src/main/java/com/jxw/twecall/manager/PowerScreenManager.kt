package com.jxw.twecall.manager

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import com.jxw.basemodel.base.IBaseManager
import com.jxw.twecall.utils.LogUtil
import java.lang.ref.WeakReference

/**
 *
 * @author: jyq
 * @desc: 控制屏幕亮度和息屏
 * @date: 2024/12/30
 */
internal object PowerScreenManager : IBaseManager {
    private const val TAG = "PowerScreenManager"
    private var wakeLock: PowerManager.WakeLock? = null

    @SuppressLint("StaticFieldLeak")
    private var mContext: WeakReference<Context>? = null

    override fun init(context: Context) {
        this.mContext = WeakReference(context)
    }


    /**
     * 设置开启多久不息屏
     * @param seconds 不设置的话 默认十分钟
     */
    fun startAcquire(seconds: Long = 10 * 60 * 1000L) {
        LogUtil.e(TAG, "PowerScreenManager startAcquire wakeLock =$wakeLock")
        if (wakeLock == null) {
            val pm = mContext?.get()?.getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = pm?.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "Yuliao::WakeLockTag"
            )
        }
        wakeLock?.acquire(seconds)
    }

    /**
     * 当不再需要保持屏幕常亮时
     */
    fun stopAcquire() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
                wakeLock = null
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "PowerScreenManager 调用stopAcquire 异常$e")
        }

    }

    override fun clear(context: Context?) {
        stopAcquire()
    }
}