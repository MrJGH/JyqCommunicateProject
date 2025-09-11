package com.jxw.basemodel.network

import android.util.Log
import androidx.annotation.IntDef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rxhttp.wrapper.exception.ParseException
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException


// 通用错误  这里暂定700
const val COMMON_ERROR = 500

@Retention(AnnotationRetention.SOURCE)
@IntDef(*[COMMON_ERROR])
annotation class ErrorCode

/**
 * 数据体 是data 时调用
 * {
 *  code:0
 *  msg:"成功"
 *  data:{
 *       ...
 *      }
 * }
 */
suspend inline fun <T> safeApiCall(
    callback: IApiErrorCallbackImpl.() -> Unit,
    crossinline responseFunction: suspend () -> T
): T? {
    val config = IApiErrorCallbackImpl().apply {
        callback()
    }
    return try {
        val response = withContext(Dispatchers.IO) {
            responseFunction.invoke()
        }
        response
    } catch (e: Throwable) {
        requestException(e, config)
        null
    }
}

/**
 * 请求异常的处理
 */
suspend fun requestException(
    e: Throwable,
    callback: IApiErrorCallbackImpl
) {
    withContext(Dispatchers.Main) {
        Log.e("requestException", e.message + e.cause)
        when (e) {
            is ParseException -> {
                callback.onParseError(e.errorCode.toInt(), "数据解析异常")
            }

           /* is IOException -> {
                callback.onNetworkError()
            }*/

            else -> {
                callback.onError(COMMON_ERROR, "其他异常")
            }
        }
    }
}

