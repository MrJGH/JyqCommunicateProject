package com.jxw.twecall.model


/**
 *
 * @author: jyq
 * @desc: 错误 消息和code
 * @date: 2024/11/11
 */
enum class ErrorInfoEnum(val errorCode: Int, val errorMsg: String) {
    //通用错误
    WXERROR_OK(0, "正常"),//操作成功
    WXERROR_CANCELLED(1, "操作被取消 通常是被调用方取消"),
    WXERROR_UNKNOWN(2, "未知错误 通常，你应该尽可能返回其他更加详细的错误码。如果实在没有相关可以使用的错误码，使用WXERROR_UNKNOWN"),
    WXERROR_INVALID_ARGUMENT(3, "非法参数 这表示方法调用方传递了非法参数"),
    WXERROR_TIMEOUT(4, "调用超时 这表示操作应该在指定时间内完成，但并未完成."),
    WXERROR_NOT_FOUND(5, "被请求的实体不存在 如文件不存在"),
    WXERROR_ALREADY_EXISTS(6, "希望创建的实体已存在 如要创建的文件已经存在"),
    WXERROR_PERMISSION_DENIED(7, "没有权限 这表示调用方无权调用指定的操作.需要与 WXERROR_UNAUTHENTICATED 进行区分."),
    WXERROR_RESOURCE_EXHAUSTED(8, "资源不足 可能表示磁盘空间已满, 或网络不连通."),
    WXERROR_FAILED_PRECONDITION(9, "前置条件不满足 这表示当前操作因系统不在可以运行的状态中而被拒绝执行. 比如要删除文件夹时，指定的路径上实际是个普通文件."),
    WXERROR_ABORTED(10, "操作被中断"),
    WXERROR_OUT_OF_RANGE(11, "超出范围 这表示当前操作超出了允许的范围，如读取文件时超出了文件长度."),
    WXERROR_UNIMPLEMENTED(12, "操作未实现 这表示当前操作尚未被实现/被支持. 因此当前操作不应该重试."),
    WXERROR_INTERNAL(13, "内部错误 这表示当前操作因为系统状态不正常而失败. 这通常表示系统内部有 bug."),
    WXERROR_UNAVAILABLE(14, "服务不可用 这表示操作尚不可用.比如要播放音频时，音频设备未连接；或者要采集音频时，话筒设备未连接."),
    WXERROR_DATA_LOSS(15, "数据丢失"),
    WXERROR_UNAUTHENTICATED(16, "没有权限"),
    WXERROR_IO(17, "IO 错误 一般是文件创建、读、写。"),
    WXERROR_RESPONSE(18, "回复错误 一般是网络返回内容不对"),
    WXERROR_INVALID_DEVICEID(19, "设备信息不匹配 一般是传入的 sn、modelid 与原注册信息不匹配"),

    //注册错误
    REGISTER_SN_TICKET_ERROR(-10008, "snticket 有问题，常见原因是 snticket 过期");


    companion object {
        fun getMessageByCode(errorCode: Int): String {
            return values().find { it.errorCode == errorCode }?.errorMsg ?: ""
        }
    }
}