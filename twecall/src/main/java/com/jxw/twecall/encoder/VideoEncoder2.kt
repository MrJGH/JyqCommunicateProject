package com.jxw.twecall.encoder

import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecCapabilities
import android.media.MediaCodecList
import android.media.MediaFormat
import com.jxw.twecall.model.VideoEncodeParam
import com.jxw.twecall.utils.LogUtil
import com.jxw.twecall.utils.YuvUtils
import java.util.Arrays

/**
 *
 * @author: jyq
 * @desc: 视频编码
 * @date: 2025/3/25
 */
class VideoEncoder2(val param: VideoEncodeParam) : BaseVideoEncoder(param) {

    override fun initVideoMediaFormat(): MediaFormat {
        return MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, param.width, param.height).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, param.bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, param.frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, param.iFrameInterval)
            setInteger(MediaFormat.KEY_COLOR_FORMAT, CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
            setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
            setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel3)
        }
    }

    override fun initMediaCodecInfo(): MediaCodecInfo? {
        val mediaCodecInfo = MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.firstOrNull { it.name == "OMX.ARM.video.v5xx.encoder" }
        val supportColorFormats = Arrays.toString(mediaCodecInfo?.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC)?.colorFormats)
        val supportProfileLevels = mediaCodecInfo?.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC)?.profileLevels
        LogUtil.d(TAG, "softwareEncoderName=${mediaCodecInfo?.name}  supportColorFormats=$supportColorFormats")
        supportProfileLevels?.forEach { LogUtil.d(TAG, "supportProfileLevels level=${it.level} profile=${it.profile}") }
        return mediaCodecInfo
    }

    override fun handleBeforeEncode(data: ByteArray): ByteArray {
        return YuvUtils.nv21ToNv12Rotated(data, param.width, param.height, YuvUtils.ROTATE_0)
    }


    companion object {
        private const val TAG = "VideoEncoder2"
    }
}