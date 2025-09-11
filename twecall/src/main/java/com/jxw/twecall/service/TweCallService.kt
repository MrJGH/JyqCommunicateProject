package com.jxw.twecall.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.jxw.basemodel.utils.LauncherDataUtil
import com.jxw.twecall.TweCallManager
import com.jxw.twecall.utils.LogUtil

/**
 *
 * @author: jyq
 * @desc: TweCallService
 * @date: 2024/11/28
 */
class TweCallService : Service() {
    companion object {
        private const val TAG = "TweCallService"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        LogUtil.e(TAG,"TweCallService onCreate")
        val notification = createForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createForegroundNotification(): Notification {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = "notification_channel_id_01"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "TRTC Foreground Service Notification"
            val notificationChannel = NotificationChannel(
                notificationChannelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "Channel description"
            notificationManager?.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(this, notificationChannelId)
        return builder.build()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (LauncherDataUtil.isNeedSendAudioAndVideo()) {
//            MqttManager.init(context =this)
            TweCallManager.init(this)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    /*override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }*/

    override fun onDestroy() {
        super.onDestroy()
    }
}