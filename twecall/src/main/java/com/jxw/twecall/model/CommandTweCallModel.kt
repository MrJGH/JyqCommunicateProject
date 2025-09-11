package com.jxw.twecall.model

/**
 *
 * @author: jyq
 * @desc: 自定义信令 音视频通话详情model
 * @date: 2025/2/13
 */
data class CommandTweCallModel(
    val userHeader: String,
    val fromAccount: String? = null,
    val toAccount: String? = null,
    val userName: String = "",
    val type: String?,
    val businessID: String = "custom_twe_call"
)

/**
 * 信令响应 model
 * @param status 返回 0 为成功 其他为失败
 */
data class CommandResponseModel(
    val status:String
)
