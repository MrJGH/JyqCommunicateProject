package com.jxw.twecall.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jxw.twecall.manager.AppLifecycleObserverManager.isForegroundTag
import com.jxw.twecall.model.MqttMessageModel
import com.jxw.twecall.ui.TweCallActivity

/**
 *
 * @author: jyq
 * @desc:  消息通知
 * @date: 2024/12/02
 */
object NotificationUtils {

    fun showNotification(context: Context, message: MqttMessageModel) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (!isForegroundTag) {
            val pendingIntent = getPendingIntent(context, message)
            val notificationId = 1
            val notificationChannelId = "kdxxj_watch_yuliao_id"
            val channelName = "Watch_Yuliao_Channel_Name"
            var contentTile: String? = ""
        /*    contentTile = if (message.isGroup()) {
                context.resources.getString(R.string.home_group)
            } else {
                message.getNickName()
            }*/
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, notificationChannelId)
//                .setSmallIcon(com.jxw.basemodel.R.drawable.ic_launcher_icon)
                .setContentTitle(contentTile)
//                .setContentText(message.avatarUrl)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (notificationManager != null && notificationManager.getNotificationChannel(notificationChannelId) == null) {
                    val channel = NotificationChannel(
                        notificationChannelId,
                        channelName,
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationManager.createNotificationChannel(channel)
                }
            }
            val notification = builder.build()
            val notificationManagerCompat = NotificationManagerCompat.from(context)
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            notificationManagerCompat.notify(notificationId, notification)
        }
    }

    private fun getPendingIntent(context: Context, message: MqttMessageModel): PendingIntent {
        val intent: Intent  = Intent(context,TweCallActivity::class.java)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}