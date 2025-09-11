package com.jxw.basemodel.network

import com.jxw.basemodel.base.EmptyListener
import com.jxw.basemodel.base.ErrorListener2

/**
 * dsl 网络请求异常接口
 */
interface IApiErrorCallback {
    fun onNetworkError()
    fun onError(@ErrorCode code: Int, msg: String)
    fun onParseError(@ErrorCode code: Int, msg: String)
}
/**
 * dsl 网络请求异常接口实现类
 */
class IApiErrorCallbackImpl : IApiErrorCallback {

    private var onNetworkErrorCallback: EmptyListener = null
    private var onErrorCallback: ErrorListener2 = null
    private var onParseErrorCallback: ErrorListener2 = null

    fun onNetworkError(callback: EmptyListener) {
        this.onNetworkErrorCallback = callback
    }

    fun onError(callback: ErrorListener2) {
        this.onErrorCallback = callback
    }
    fun onParseError(callback: ErrorListener2) {
        this.onParseErrorCallback = callback
    }


    override fun onNetworkError() {
        onNetworkErrorCallback?.invoke()
    }

    override fun onError(code: Int, msg: String) {
        onErrorCallback?.invoke(code, msg)
    }

    override fun onParseError(code: Int, msg: String) {
        onParseErrorCallback?.invoke(code,msg)
    }
}