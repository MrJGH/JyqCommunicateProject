package com.tencent.qcloud.tuikit.tuichat.util

import android.content.Context
import android.content.Intent
import android.os.Build


object BroadcastUtils {

    // 默认广播Action常量
    private const val ACTION_STOP_RECORD: String = "aidzcw.stopRecord"
    private const val ACTION_START_RECORD: String = "aidzcw.startRecord"
    /**
     * 针对ai学习搭子项目使用 (原因：ai学习搭子 小九语音唤醒，一直占用录音 ，当使用录音功能的时候，需要通知小九是释放，并且不使用的时候需要通知小九去恢复)
     */
    fun sendStartRecordBroadcast(context: Context) {
        val intent = Intent(ACTION_START_RECORD).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setPackage("com.jxw.launcher")
            }
        }
        context.sendBroadcast(intent)
    }
    /**
     * 针对ai学习搭子项目使用 (原因：ai学习搭子 小九语音唤醒，一直占用录音 ，当使用录音功能的时候，需要通知小九是释放，并且不使用的时候需要通知小九去恢复)
     */
    fun sendStopRecordBroadcast(context: Context) {
        val intent = Intent(ACTION_STOP_RECORD).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setPackage("com.jxw.launcher")
            }
        }
        context.sendBroadcast(intent)
    }
}
