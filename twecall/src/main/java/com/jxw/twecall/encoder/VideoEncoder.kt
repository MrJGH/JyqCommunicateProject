package com.jxw.twecall.encoder

import android.media.MediaCodecInfo
import android.media.MediaFormat
import com.jxw.basemodel.utils.LauncherDataUtil.getCameraBackFrontId
import com.jxw.twecall.model.VideoEncodeParam
import com.jxw.twecall.utils.YuvUtils
import com.jxw.twecall.utils.YuvUtils.nv21ToNv12Rotated

class VideoEncoder(private val videoEncodeParam: VideoEncodeParam) : BaseVideoEncoder(videoEncodeParam) {
    private var mCameraId = 0

    fun setCameraId(mCameraId: Int) {
        this.mCameraId = mCameraId
    }

    override fun initVideoMediaFormat(): MediaFormat {
        return MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, videoEncodeParam.width, videoEncodeParam.height).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, videoEncodeParam.bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, videoEncodeParam.frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, videoEncodeParam.iFrameInterval)
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
            setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
            setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel3)
        }
    }

    /**
     * 将NV21编码成H264
     */
    override fun handleBeforeEncode(data: ByteArray): ByteArray {
        return if (mCameraId == getCameraBackFrontId()) {
            nv21ToNv12Rotated(data, videoEncodeParam.width, videoEncodeParam.height, YuvUtils.ROTATE_180)
        } else {
            nv21ToNv12Rotated(data, videoEncodeParam.width, videoEncodeParam.height, YuvUtils.ROTATE_0)
        }
    }
}