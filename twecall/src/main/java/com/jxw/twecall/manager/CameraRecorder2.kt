package com.jxw.twecall.manager

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import com.jxw.basemodel.utils.LauncherDataUtil.getCameraBackFrontId
import com.jxw.basemodel.utils.LauncherDataUtil.getCameraFacingFrontId
import com.jxw.basemodel.utils.LauncherDataUtil.getCameraPreviewHeightSize
import com.jxw.basemodel.utils.LauncherDataUtil.getCameraPreviewWidthSize
import com.jxw.basemodel.utils.LauncherDataUtil.getAudioEncoderType
import com.jxw.basemodel.utils.LauncherDataUtil.getVideoEncoderType
import com.jxw.basemodel.utils.LauncherDataUtil.getDefaultSelectCameraId
import com.jxw.twecall.encoder.AudioEncoder1
import com.jxw.twecall.encoder.BaseAudioEncoder
import com.jxw.twecall.encoder.BaseVideoEncoder
import com.jxw.twecall.encoder.VideoEncoder
import com.jxw.twecall.encoder.VideoEncoder0
import com.jxw.twecall.encoder.VideoEncoder1
import com.jxw.twecall.encoder.VideoEncoder2
import com.jxw.twecall.encoder.VideoEncoder3
import com.jxw.twecall.listener.OnEncodeListener
import com.jxw.twecall.model.CallMediaType
import com.jxw.twecall.model.DynamicBitRateType
import com.jxw.twecall.model.VideoEncodeParam
import com.jxw.twecall.utils.CameraUtils
import com.jxw.twecall.utils.LogUtil
import com.jxw.twecall.utils.toDateString
import com.tencent.iot.video.device.VideoNativeInterface
import com.tencent.iot.video.device.annotations.VideoResType
import java.io.IOException
import java.util.Timer
import java.util.TimerTask

/**
 *
 * @author: jyq
 * @desc: 视频录制
 * @date: 2025/3/25
 */
internal class CameraRecorder2 : PreviewCallback, OnEncodeListener {
    private var mCameraId = getDefaultSelectCameraId()
    private var mVideoWidth = getCameraPreviewWidthSize()
    private var mVideoHeight = getCameraPreviewHeightSize()
    private val mVideoFrameRate = 15
    private var mCamera: Camera? = null
    private var mVideoEncoder: BaseVideoEncoder? = null
    private var mAudioEncoder: BaseAudioEncoder? = null
    private var isMuted = false
    private var mIsRecording = false
    private val mVisitorInfo: MutableMap<Int, Int> = HashMap(MaxVisitors)

    @VideoResType
    private var videoResType = 0
    private var visitor = 0
    private var isRunning = false

    // for test only
    private var isSaveRecord = false
    private var mSaveAudioAndVideoManager: SaveAudioAndVideoManager? = null

    private var mTurnCameraMirrorManager: TurnCameraMirrorManager? = null

    /**
     * 保存 相机预览的视频和 编码后的视频到本地 调试时使用
     */
    fun isSaveRecord(isSaveRecord: Boolean) {
        this.isSaveRecord = isSaveRecord
        mSaveAudioAndVideoManager = SaveAudioAndVideoManager()
        mSaveAudioAndVideoManager?.startSavingFrames()
    }

    fun openCamera(surfaceTexture: SurfaceTexture?, context: Context?) {
        try {
            mCamera = Camera.open(mCameraId)?.apply {
                setDisplayOrientation(CameraUtils.getDisplayOrientation(context, mCameraId))
                parameters = getParameters().apply {
                    if (parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                    } else if (parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                    }
                    parameters.supportedPreviewSizes.forEach {
                        LogUtil.w(TAG, " Camera.Parameters.supportedPreviewSize() width=" + it.width + "  height=" + it.height)
                    }
                    LogUtil.w(TAG, " Camera.Parameters.supportedPreviewFormats()" + parameters.supportedPreviewFormats)
                    setPreviewSize(mVideoWidth, mVideoHeight)
                    previewFormat = ImageFormat.NV21
                    previewFrameRate = mVideoFrameRate
                }
                setPreviewTexture(surfaceTexture)
                setPreviewCallback(this@CameraRecorder2)
                startPreview()
            }
            isRunning = true
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun closeCamera() {
        try {
            mVideoEncoder?.release()
            mCamera?.let {
                it.stopPreview()
                it.setPreviewCallback(null)
                it.release()
                mCamera = null
            }
            isRunning = false
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    fun startRecording(visitor: Int, res_type: Int) {
        videoResType = res_type
        this.visitor = visitor
        if (mIsRecording) {
            mVisitorInfo[visitor] = res_type
            return
        }
        mVisitorInfo[visitor] = res_type
        startVideoEncoder()
        startAudioEncoder()
        mIsRecording = true
        LogUtil.d(TAG, "start camera recording")
        startBitRateAdapter()
    }

    /**
     * 开启执行 音频编码
     */
    private fun startAudioEncoder() {
        when (getAudioEncoderType()) {
            1 -> mAudioEncoder = AudioEncoder1()
            else -> mAudioEncoder = BaseAudioEncoder()
        }
        mAudioEncoder?.apply {
            setOnEncodeListener(this@CameraRecorder2)
            setMuted(isMuted)
            startAudio()
        }
    }

    /**
     * 开始 视频编码
     */
    private fun startVideoEncoder() {
        if (TweCallStateManager.callMediaType == CallMediaType.Video) {
            val encodeParam = VideoEncodeParam().apply {
                height = mVideoHeight
                width = mVideoWidth
                frameRate = mVideoFrameRate
                bitRate = getAverageBitRate()
            }
            mVideoEncoder = when (getVideoEncoderType()) {
                0 -> VideoEncoder0(encodeParam)
                1 -> VideoEncoder1(encodeParam)
                2 -> VideoEncoder2(encodeParam)
                3 -> VideoEncoder3(encodeParam)
                else -> VideoEncoder(encodeParam).apply { setCameraId(mCameraId) }
            }
            mVideoEncoder?.apply {
                setEncoderListener(this@CameraRecorder2)
                start()
            }
        } else {
            LogUtil.e(TAG, "当前通讯不是视频类型")
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        mAudioEncoder?.setMuted(isMuted)
    }

    fun stopRecording(visitor: Int, res_type: Int) {
        if (!mIsRecording) {
            return
        }
        mVideoEncoder?.stop()
//        mAudioEncoder?.stopRecord()
        mAudioEncoder?.stopAudio()
        mIsRecording = false
        mVisitorInfo.remove(visitor)
        if (mVisitorInfo.isNotEmpty()) return
        LogUtil.d(TAG, "stop camera recording")
        stopBitRateAdapter()
    }

    override fun onAudioEncoded(datas: ByteArray, pts: Long, seq: Long) {
//        LogUtil.d(TAG, "encoded audio data len " + datas.length + " pts " + pts);
        if (mIsRecording) {
            mVisitorInfo.forEach { (visitor, videoResType) ->
                val ret = VideoNativeInterface.getInstance().sendAvtAudioData(datas, pts, seq, visitor, 0, videoResType)
                if (ret != 0) {
                    LogUtil.e(TAG, "sendAudioData to visitor $visitor failed: $ret")
                }
            }
            if (isSaveRecord) {
                mSaveAudioAndVideoManager?.saveLocalEncodedAudio(datas)
            }
        }
    }

    private var stat_cnt = 0
    override fun onVideoEncoded(datas: ByteArray, pts: Long, seq: Long, isKeyFrame: Boolean) {
//        LogUtil.e(TAG, "encoded video data len " + datas.size + " pts " + pts)
        if (mIsRecording) {
            mVisitorInfo?.forEach { (visitor, videoResType) ->
                val iv = VideoNativeInterface.getInstance()
                val ret = iv.sendAvtVideoData(datas, pts, seq, isKeyFrame, visitor, 0, videoResType)
                if (ret != 0) {
                    val buf_size = iv.getSendStreamBuf(visitor, 0, videoResType)
                    LogUtil.e(TAG, "sendVideoData to visitor $visitor failed: $ret buf size $buf_size")
                }
                if (stat_cnt++ % 50 == 0) {
                    val buf_size = iv.getSendStreamBuf(visitor, 0, videoResType)
                    val ivP2pSendInfo = iv.getSendStreamStatus(visitor, 0, videoResType)
                    LogUtil.d(
                        TAG,
                        "visitor " + visitor + " buf size " + buf_size + " link mode " + ivP2pSendInfo.linkMode + "  instNetRate:" + ivP2pSendInfo.instNetRate + "   aveSentRate:" + ivP2pSendInfo.aveSentRate + "   sumSentAcked:" + ivP2pSendInfo.sumSentAcked
                    )
                }
                if (isSaveRecord) {
                    mSaveAudioAndVideoManager?.saveLocalEncodedVideo(datas)
                }
            }
        }
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera) {
        if (!mIsRecording || mVideoEncoder == null) {
            LogUtil.e(TAG, "onPreviewFrame mIsRecording is false or mVideoEncoder is null")
            return
        }
        if (data == null) {
            LogUtil.e(TAG, "onPreviewFrame data is null 视频过程中设备没有电或者其他异常情况")
            return
        }
        mVideoEncoder?.encoderH264(data)
        if (isSaveRecord) {
            mSaveAudioAndVideoManager?.saveLocalPreViewVideo(data)
            mSaveAudioAndVideoManager?.saveLocalPreViewVideoBitMap(data)
        }

    }

    @DynamicBitRateType
    private val dynamicBitRateType = DynamicBitRateType.INTERNET_SPEED_TYPE

    inner class AdapterBitRateTask : TimerTask() {
        private var exceedLowMark = false
        override fun run() {
            println("检测时间到:" + System.currentTimeMillis().toDateString())
            mVideoEncoder?.let {
                if (dynamicBitRateType == DynamicBitRateType.WATER_LEVEL_TYPE) {
                    val bufSize = VideoNativeInterface.getInstance().getSendStreamBuf(visitor, 0, videoResType)
                    val p2p_wl_avg = VideoNativeInterface.getInstance().getAvgMaxMin(bufSize)
                    val now_video_rate = it.getVideoBitRate()
                    val now_frame_rate = it.getVideoFrameRate()
                    LogUtil.e(
                        TAG,
                        "WATER_LEVEL_TYPE send_bufsize==$bufSize,now_video_rate==$now_video_rate,avg_index==$p2p_wl_avg,now_frame_rate==$now_frame_rate"
                    )
                    // 降码率
                    // 当发现p2p的水线超过一定值时，降低视频码率，这是一个经验值，一般来说要大于 [视频码率/2]
                    // 实测设置为 80%视频码率 到 120%视频码率 比较理想
                    // 在10组数据中，获取到平均值，并将平均水位与当前码率比对。
                    val video_rate_byte = now_video_rate / 8 * 3 / 4
                    if (p2p_wl_avg > video_rate_byte) {
                        it.setVideoBitRate(now_video_rate / 2)
                        it.setVideoFrameRate(now_frame_rate / 3)
                    } else if (p2p_wl_avg < now_video_rate / 8 / 3) {

                        // 升码率
                        // 测试发现升码率的速度慢一些效果更好
                        // p2p水线经验值一般小于[视频码率/2]，网络良好的情况会小于 [视频码率/3] 甚至更低
                        it.setVideoBitRate(now_video_rate + (now_video_rate - p2p_wl_avg * 8) / 5)
                        it.setVideoFrameRate(now_frame_rate * 5 / 4)
                    }
                } else if (dynamicBitRateType == DynamicBitRateType.INTERNET_SPEED_TYPE) {
                    val ivP2pSendInfo = VideoNativeInterface.getInstance().getSendStreamStatus(visitor, 0, videoResType)
                    val bufSize = VideoNativeInterface.getInstance().getSendStreamBuf(visitor, 0, videoResType)
                    val now_video_rate = it.getVideoBitRate()
                    val now_frame_rate = it.getVideoFrameRate()
                    val nowBitRateInterval = it.getBitRateInterval()
                    LogUtil.d(
                        TAG,
                        "INTERNET_SPEED_TYPE bufsize=" + bufSize + " video_rate/8*0.8=" + now_video_rate / 8 * 0.8 + "  video_rate=" + now_video_rate + "  frame_rate=" + now_frame_rate
                    )
                    var new_video_rate = 0
                    var new_frame_rate = 0
                    //判断当前码率/8和网速，如果码率/8大于当前网速，并且两次水位值都大于20k，开始降码率
                    if (ivP2pSendInfo == null) return
                    exceedLowMark = bufSize > 20 * 1024
                    if (ivP2pSendInfo.aveSentRate < now_video_rate.toDouble() / 8 * 0.9 && exceedLowMark) {
                        // 降码率
                        new_video_rate = (now_video_rate * 0.75).toInt()
                        new_frame_rate = now_frame_rate * 4 / 5
                    } else if (bufSize < 20 * 1024) { //当前水位值小于20k，开始升码率
                        if (now_video_rate < nowBitRateInterval.last / 2) {
                            new_video_rate = (now_video_rate * 1.1).toInt()
                            new_frame_rate = now_frame_rate * 5 / 4
                        }
                    } else {
                        return
                    }
                    if (new_video_rate < nowBitRateInterval.first && now_video_rate > nowBitRateInterval.first) {
                        new_video_rate = (now_video_rate * 0.8f).toInt()
                    } else if (new_video_rate > nowBitRateInterval.last && now_video_rate < nowBitRateInterval.first) {
                        new_video_rate = (now_video_rate * 1.1f).toInt()
                    }
                    if (new_video_rate != 0) {
                        it.setVideoBitRate(new_video_rate)
                    }
                    if (new_frame_rate != 0) {
                        it.setVideoFrameRate(new_frame_rate)
                    }
                    LogUtil.d(TAG, "new_video_rate:" + new_video_rate + "  VideoBitRate:" + it.getVideoBitRate())
                }
            }
        }
    }

    private fun startBitRateAdapter() {
        bitRateTimer = Timer().apply {
            schedule(AdapterBitRateTask(), 3000, 1000)
        }
    }

    private fun stopBitRateAdapter() {
        bitRateTimer?.let {
            it.cancel()
            bitRateTimer = null
        }
    }

    fun switchCamera(surfaceTexture: SurfaceTexture?, context: Context?) {
        stopRecording(visitor, videoResType)
        closeCamera()
        mCameraId = if (mCameraId == getCameraBackFrontId()) getCameraFacingFrontId() else getCameraBackFrontId()
        openCamera(surfaceTexture, context)
        startRecording(visitor, videoResType)
        if (mVideoEncoder != null && mVideoEncoder is VideoEncoder) {
            (mVideoEncoder as VideoEncoder).setCameraId(mCameraId)
        }
    }

    fun switchCameraMirror(surfaceTexture: SurfaceTexture?, context: Context?) {
        if (mTurnCameraMirrorManager == null) {
            mTurnCameraMirrorManager = TurnCameraMirrorManager()
        }
        mTurnCameraMirrorManager?.turnCameraMirror()
        stopRecording(visitor, videoResType)
        closeCamera()
        openCamera(surfaceTexture, context)
        startRecording(visitor, videoResType)
    }

    fun release() {
        try {
            stopRecording(visitor, videoResType)
            closeCamera()
            mSaveAudioAndVideoManager?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "CameraRecorder2"
        private const val MaxVisitors = 4
        private var bitRateTimer: Timer? = null
    }
}