package com.jxw.twecall.utils

import android.util.Log

/**
 *
 * @author: jyq
 * @desc: 日志工具类
 * @date: 2024/10/31
 */
object LogUtil {
    private const val TAG = "Twecall"
    var DEBUG: Boolean = false


    fun initLog(isDebug: Boolean) {
        DEBUG = isDebug
    }

    fun i(tag: String = TAG, msg: String) {
        if (DEBUG) {
            Log.i(tag, msg)
        }
    }

    fun d(tag: String = TAG, msg: String) {
        if (DEBUG) {
            Log.d(tag, msg)
        }
    }

    fun w(tag: String = TAG, msg: String) {
        if (DEBUG) {
            Log.w(tag, msg)
        }
    }

    fun e(tag: String = TAG, msg: String) {
        if (DEBUG) {
            Log.e(tag, msg)
        }
    }
}