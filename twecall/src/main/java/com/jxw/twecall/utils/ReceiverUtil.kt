package com.jxw.twecall.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter

/**
 *
 * @author: jyq
 * @desc: 注册广播工具类
 * @date: 2024/12/20
 */
object ReceiverUtil {
    fun register(context: Context, receiver: BroadcastReceiver?, vararg actions: String?) {
        val filter = IntentFilter()
        for (action in actions) {
            filter.addAction(action)
        }
        context.registerReceiver(receiver, filter)
    }

    fun unRegister(context: Context, receiver: BroadcastReceiver?) {
        context.unregisterReceiver(receiver)
    }
}