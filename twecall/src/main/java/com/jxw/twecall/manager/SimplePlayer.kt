package com.jxw.twecall.manager

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.Surface
import com.jxw.basemodel.utils.LauncherDataUtil.getVideoEncoderType
import com.jxw.twecall.decoder.BaseAudioDecoder
import com.jxw.twecall.decoder.BaseVideoDecoder
import com.jxw.twecall.model.VideoDecodeParam
import java.io.IOException

internal class SimplePlayer {
    private var videoDecoder: BaseVideoDecoder? = null
    private var audioDecoder: BaseAudioDecoder? = null
    var context: Context? = null

    // for test only
    private var isSaveRecord = false
    private var mSaveAudioAndVideoManager: SaveAudioAndVideoManager? = null

    /**
     * 保存 解码后的视频到本地 调试时使用
     */
    fun isSaveRecord(isSaveRecord: Boolean) {
        this.isSaveRecord = isSaveRecord
        mSaveAudioAndVideoManager = SaveAudioAndVideoManager()
    }

    fun init(context: Context?) {
        this.context = context
    }

    /**
     * 开始视频播放（currently only support h.264）
     * @param type   0: h.264/avc   1: h.265/hevc
     */
    fun startVideoPlay(surface: Surface?, visitor: Int, type: Int, height: Int, width: Int): Int {
        Log.d(TAG, "startVideoPlay visitor " + visitor + " height " + height + " width " + width + ", model:" + Build.MODEL + " type =" + type)
        if (type == 0 && height > 0 && width > 0 && surface != null) {
            try {
                videoDecoder = BaseVideoDecoder(VideoDecodeParam(width = width, height = height, surface = surface))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return 0
    }

    /**
     * 开始播放音频（currently only support AAC or PCM）
     * @param type  0: PCM
     * @param type  4并且 option=2 :aac-lc
     */
    fun startAudioPlay(visitor: Int, type: Int, option: Int, mode: Int, width: Int, sample_rate: Int, sample_num: Int): Int {
        Log.d(TAG, "audio input from visitor $visitor type $type option $option  getEncoderType()=${getVideoEncoderType()}")
        if (type != 4 && type != 0) {
            Log.e(TAG, "unsupported audio format $type")
            return -1
        }
        if (audioDecoder == null) {
            audioDecoder = BaseAudioDecoder()
        }
        audioDecoder?.startAudio(type, mode, width, sample_rate)
        return 0
    }

    fun stopVideoPlay(visitor: Int): Int {
        return try {
            Log.d(TAG, "visitor $visitor stop video play")
            videoDecoder?.stopVideo()
            videoDecoder = null
            0
        } catch (t: Throwable) {
            t.printStackTrace()
            -1
        }
    }

    fun stopAudioPlay(visitor: Int): Int {
        Log.d(TAG, "visitor $visitor stop audio play")
        // 这里停止可能导致音频没有播放完就退出？
        audioDecoder?.stopAudio()
        audioDecoder = null
        return 0
    }

    fun playVideoStream(visitor: Int, data: ByteArray, len: Int, pts: Long, seq: Long): Int {
//        Log.d(TAG, "video frame: visitor "+ visitor + " len " + len + " pts " + pts + " seq " + seq);
//        Log.e(TAG, "videoDecoder.playVideoStream() pts$pts")
        if (isSaveRecord && data != null) {
            mSaveAudioAndVideoManager?.saveLocalDecodedVideo(data)
        }
        return videoDecoder?.decoderH264(data, len, pts) ?: 0
    }

    fun playAudioStream(visitor: Int, data: ByteArray?, len: Int, pts: Long, seq: Long): Int {
//        Log.d(TAG, "audio frame: visitor "+ visitor + " len " + len + " pts " + pts + " seq " + seq);
        return data?.let { audioDecoder?.decoderAAC(it, len, pts) } ?: 0
        /* audioDecoder?.let {
             if (videoDecoder != null) {
                 Log.e(TAG, "videoDecoder.getCurrentVideoPts()" + videoDecoder!!.currentVideoPts)
                 it.setCurrentVideoPts(videoDecoder!!.currentVideoPts)
             }
             return it.decoderAAC(data!!, len, pts)
         }
         return 0*/
    }

    fun release() {
        stopAudioPlay(0)
        stopVideoPlay(0)
    }

    companion object {
        private const val TAG = "SimplePlayer"
    }
}