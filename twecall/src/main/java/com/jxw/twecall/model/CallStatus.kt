package com.jxw.twecall.model

/**
 *
 * @author: jyq
 * @desc:  通讯状态
 * @date: 2024/11/18
 */
enum class CallStatus {
    None,
    Waiting,
    AudioAccept,
    VideoAccept,
    HangUp
}