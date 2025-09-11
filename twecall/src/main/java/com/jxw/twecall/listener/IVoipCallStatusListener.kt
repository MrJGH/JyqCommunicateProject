package com.jxw.twecall.listener

/**
 *
 * @author: jyq
 * @desc: 呼叫回调状态监听
 * @date: 2024/11/5
 */
interface IVoipCallStatusListener {
    /**
     * 对方通话进行中
     * @param callStatus 通话状态：-2
     * @param callMessage 通话状态：通话进行中
     */
    fun onLineBusy(callStatus: Int, callMessage: String? = "")

    /**
     * 呼叫成功
     */
    fun callSuccess(callStatus: Int, callMessage: String? = "")

    /**
     * 呼叫失败
     */
    fun callFail(callStatus: Int, callMessage: String? = "")

}