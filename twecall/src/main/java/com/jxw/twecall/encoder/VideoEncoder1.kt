package com.jxw.twecall.encoder

import android.media.MediaCodecInfo
import android.media.MediaFormat
import com.jxw.twecall.model.VideoEncodeParam

/**
 *
 * @author: jyq
 * @desc: 视频编码
 * @date: 2025/3/25
 */
class VideoEncoder1(val param: VideoEncodeParam) : BaseVideoEncoder(param) {

    override fun initVideoMediaFormat(): MediaFormat {
        return MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, param.width, param.height).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, param.bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, param.frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, param.iFrameInterval)
            setInteger(MediaFormat.KEY_COLOR_FORMAT, 2135033992)
            setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
            setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel3)
        }
    }
}