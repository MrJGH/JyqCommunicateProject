package com.jxw.twecall.listener

import com.jxw.twecall.utils.LogUtil
import com.tencent.iot.video.device.callback.IvAvtCallback
import com.tencent.iot.video.device.consts.CommandType
import com.tencent.iot.video.device.model.AvDataInfo

/**
 *
 * @author: jyq
 * @desc: 音视频相关响应回调
 * @date: 2024/10/31
 */
interface AvtListener : IvAvtCallback {
    /**
     * 获取音视频编码参数信息
     */
    override fun onGetAvEncInfo(visitor: Int, channel: Int, videoResType: Int): AvDataInfo = AvDataInfo.createDefaultAvDataInfo(videoResType)

    /**
     *现场音视频开始播放回调
     */
    override fun onStartRealPlay(visitor: Int, channel: Int, videoResType: Int) {}

    /**
     * 现场音视频停止播放回调
     */
    override fun onStopRealPlay(visitor: Int, channel: Int, videoResType: Int) {}

    /**
     * 开始接收音频数据流回调
     */
    override fun onStartRecvAudioStream(
        visitor: Int,
        channel: Int,
        type: Int,
        option: Int,
        mode: Int,
        width: Int,
        sample_rate: Int,
        sample_num: Int
    ): Int = 0

    /**
     * 开始接收视频数据流回调
     */
    override fun onStartRecvVideoStream(visitor: Int, channel: Int, type: Int, height: Int, width: Int, frameRate: Int): Int = 0

    /**
     * 通知设备停止接收对向数据流的回调，结束收流的回调只会被调用一次
     */
    override fun onStopRecvStream(visitor: Int, channel: Int, streamType: Int): Int = 0

    /**
     * 接收对向的数据流回调。用于观看端向设备发送音视频数据时，设备端的接收数据回调，由用户实现对数据进行处理播放。
     */
    override fun onRecvStream(visitor: Int, streamType: Int, data: ByteArray, len: Int, pts: Long, seq: Long): Int = 0

    /**
     * 监控过程中，事件通知接口，用户在该回调中可以根据事件类型做相应的操作。当前版本主要是拥塞控制的事件回调;
     */
    override fun onNotify(event: Int, visitor: Int, channel: Int, videoResType: Int) {}

    /**
     * 接收信令处理回调
     */
    override fun onRecvCommand(command: Int, visitor: Int, channel: Int, videoResType: Int, args: String?): String {
        LogUtil.w("", "onRecvCommand command $command visitor $visitor channel$channel   videoResType$videoResType   args$args")
        var msg = ""
        msg = when (command) {
            CommandType.IV_AVT_COMMAND_USR_DATA -> {
                LogUtil.d(msg = "receive command: user data")
                "user data"
            }

            CommandType.IV_AVT_COMMAND_REQ_STREAM -> {
                LogUtil.d(msg = "receive command: request stream")
                "request stream"
            }

            CommandType.IV_AVT_COMMAND_CHN_NAME -> {
                LogUtil.d(msg = "receive command: get channel name")
                "get channel name"
            }

            CommandType.IV_AVT_COMMAND_REQ_IFRAME -> {
                LogUtil.d(msg = "receive command: request I frame")
                "request I frame"
            }

            CommandType.IV_AVT_COMMAND_PLAYBACK_PAUSE -> {
                LogUtil.d(msg = "receive command: playback pause")
                "playback pause"
            }

            CommandType.IV_AVT_COMMAND_PLAYBACK_RESUME -> {
                LogUtil.d(msg = "receive command: playback resume")
                "playback resume"
            }

            CommandType.IV_AVT_COMMAND_PLAYBACK_QUERY_MONTH -> {
                LogUtil.d(msg = "receive command: playback query month")
                "playback query month"
            }

            CommandType.IV_AVT_COMMAND_PLAYBACK_QUERY_DAY -> {
                LogUtil.d(msg = "receive command: playback query day")
                "playback query day"
            }

            CommandType.IV_AVT_COMMAND_PLAYBACK_SEEK -> {
                LogUtil.d(msg = "receive command: playback seek")
                "playback seek"
            }

            CommandType.IV_AVT_COMMAND_PLAYBACK_FF -> {
                LogUtil.d(msg = "receive command: playback fast forward")
                "playback fast forward"
            }

            CommandType.IV_AVT_COMMAND_PLAYBACK_SPEED -> {
                LogUtil.d(msg = "receive command: playback speed")
                "playback speed"
            }

            CommandType.IV_AVT_COMMAND_PLAYBACK_REWIND -> {
                LogUtil.d(msg = "receive command: playback rewind")
                "playback rewind"
            }

            CommandType.IV_AVT_COMMAND_PLAYBACK_PROGRESS -> {
                LogUtil.d(msg = "receive command: playback progress")
                "playback progress"
            }

            CommandType.IV_AVT_COMMAND_QUERY_FILE_LIST -> {
                LogUtil.d(msg = "receive command: get file list")
                "get file list"
            }

            CommandType.IV_AVT_COMMAND_CALL_ANSWER -> {
                LogUtil.d(msg = "receive command: call answer")
                "call answer"
            }

            CommandType.IV_AVT_COMMAND_CALL_HANG_UP -> {
                LogUtil.d(msg = "receive command: call hang up")
                "call hang up"
            }

            CommandType.IV_AVT_COMMAND_CALL_REJECT -> {
                LogUtil.d(msg = "receive command: call reject")
                "call reject"
            }

            CommandType.IV_AVT_COMMAND_CALL_CANCEL -> {
                LogUtil.d(msg = "receive command: call cancel")
                "call cancel"
            }

            CommandType.IV_AVT_COMMAND_CALL_BUSY -> {
                LogUtil.d(msg = "receive command: call busy")
                "call busy"
            }

            CommandType.IV_AVT_COMMAND_CALL_TIMEOUT -> {
                LogUtil.d(msg = "receive command: call timeout")
                "call timeout"
            }

            else -> {
                LogUtil.d(msg = "not support command")
                "unsupport cmd type $command"
            }
        }
//        updateUI(this, msg)
        return "success"
    }

    /**
     * 文件下载回调
     */
    override fun onDownloadFile(status: Int, visitor: Int, channel: Int): Int = 0

    /**
     * 获取对端外网IP信息回调
     */
    override fun onGetPeerOuterNet(visitor: Int, channel: Int, netInfo: String?) {}
}