package com.jxw.twecall.listener

/**
 *
 * @author: jyq
 * @desc: 对外暴漏的  twecall初始化监听接口
 * @date: 2024/12/17
 */
interface TweCallInitListener {
    /**
     * 初始化成功
     */
    fun initSuccess()

    /**
     * 初始进行中
     */
    fun initializing()

    /**
     * 初始化失败
     */
    fun initFail()


}