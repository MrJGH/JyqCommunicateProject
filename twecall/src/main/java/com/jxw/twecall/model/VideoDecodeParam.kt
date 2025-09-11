package com.jxw.twecall.model

import android.media.MediaFormat
import android.view.Surface

/**
 *
 * @author: jyq
 * @desc: 视频界面参数
 * @date: 2025/5/7
 */
data class VideoDecodeParam(
    var width: Int = -1,//录制宽度
    var height: Int = -1, //录制高度
    val mime: String = MediaFormat.MIMETYPE_VIDEO_AVC, // 编码格式: 默认 H264
    val surface: Surface
)
