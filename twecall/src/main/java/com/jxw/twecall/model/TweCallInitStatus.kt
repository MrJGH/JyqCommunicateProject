package com.jxw.twecall.model

/**
 *
 * @author: jyq
 * @desc: 初始化状态枚举
 * @date: 2024/12/30
 */
enum class TweCallInitStatus {
    INIT_STATUS_FAIL,  //初始化失败
    INIT_STATUS_SUCCESS,  //初始化成功
    INIT_STATUS_INITIALIZING, //初始化过程中
}