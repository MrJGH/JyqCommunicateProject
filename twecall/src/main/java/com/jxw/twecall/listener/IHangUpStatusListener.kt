package com.jxw.twecall.listener

/**
 *
 * @author: jyq
 * @desc: 挂断回调状态监听
 * @date: 2024/11/6
 */
interface IHangUpStatusListener {
    /**
     * 挂断成功
     * @param hangUpStatus 0
     * @param hangUpMessage 挂断成功
     */
    fun hangUpSuccess(hangUpStatus: Int, hangUpMessage: String?="")

    /**
     * 挂断失败
     */
    fun hangUpFail(hangUpStatus: Int, hangUpMessage: String?="")
}