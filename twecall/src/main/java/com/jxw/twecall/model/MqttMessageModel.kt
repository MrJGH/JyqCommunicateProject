package com.jxw.twecall.model

/**
 *
 * @author: jyq
 * @desc: mqtt 接收到的消息体
 * @date: 2024/11/28
 * @param type 消息类型   1 GROUP_CHAT_AUDIO  2 GROUP_CHAT_VIDEO
 * @param notifyType 消息类型   1 GROUP_CHAT_AUDIO  2 GROUP_CHAT_VIDEO
 * @param avatarUrl 头像
 * @param fromAccount 呼叫的 账号
 * @param toAccount  被呼叫的账号
 */
data class MqttMessageModel(
    val payload: String,
    val type: String
)

/**
 * @param businessID  这里需要写固定值  需要和TUIChatConstants BUSINESS_ID_CUSTOM_TWE_CALL 一致
 */
data class MqttMessageDetailModel(
    val avatarUrl: String,
    val fromAccount: String? = null,
    val toAccount: String? = null,
    val nickName: String = "",
    val notifyType: String?,
    val businessID: String = "custom_twe_call"
)

/**
 * mqtt 消息类型枚举
 */
enum class MqttMessageType(val type: String) {
    GROUP_CHAT_AUDIO("GROUP_CHAT_AUDIO"),
    GROUP_CHAT_VIDEO("GROUP_CHAT_VIDEO")
}




