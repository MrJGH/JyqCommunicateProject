package com.jxw.twecall.listener

/**
 *
 * @author: jyq
 * @desc:  wx呼叫设备监听   监听wx的状态
 * @date: 2024/11/6
 */
interface IWxCallStatusListener {
    /**
     * 小程序发起请求
     */
    fun onWxCallStart()

    /**
     * 小程序发起视频请求
     */
    fun onWxCallVideoStart()

    /**
     * 小程序发起音频请求
     */
    fun onWxCallAudioStart()

    /**
     * 小程序取消呼叫
     */
    fun onWxCallCancel()

    /**
     * 小程序挂断
     */
    fun onWxCallHangUp()

    /**
     * 小程序呼叫超时
     */
    fun onWxCallTimeOut()

}

/**
 * 设备呼叫wx监听   监听wx的状态
 */
interface IDeviceCallStatusListener{
    /**
     * wx 没有接听的情况下 （超时自动挂断，或者收到点击挂断）
     */
    fun onWxCancelCallBack()
}