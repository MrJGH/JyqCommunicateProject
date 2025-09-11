package com.jxw.twecall.manager

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.jxw.basemodel.base.BaseApplication
import com.jxw.basemodel.base.EmptyListener
import com.jxw.basemodel.base.IBaseManager
import com.jxw.twecall.utils.LogUtil
import com.jxw.twecall.utils.ext.tryCatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

/**
 *
 * @author: jyq
 * @date: 2024/9/25
 * @desc: 音频播放 管理
 */
object MediaPlayerManager : IBaseManager {
    private var mMediaPlayer: MediaPlayer? = null
    private var mContext: WeakReference<Context>? = null
    private var mJob: Job? = null
    private var mProgressCallBack: IntListener = null
    var manager: AudioManager = BaseApplication.getInstance().getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun init(context: Context) {
        mContext = WeakReference(context)
    }

    /**
     * 开始播放
     * @param url 服务器播放地址 或者 assets下文件名 如果是assets下文件名  isLocal一定是true
     * @param waitingFunction 播放前 等待回调
     * @param startPlayFunction 开始播放回调
     * @param playFinishFunction 播放完成回调
     * @param progressCallBack 播放进度回调
     * @param errorCallBack 播放错误回调
     */
    fun playStart(
        url: String,
        waitingFunction: EmptyListener = null,
        startPlayFunction: EmptyListener = null,
        playFinishFunction: EmptyListener = null,
        progressCallBack: IntListener = null,
        errorCallBack: EmptyListener = null,
        isLocal: Boolean = false,
    ) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer()
        }
        mProgressCallBack = progressCallBack
        tryCatch<MediaPlayerManager> {
            mMediaPlayer?.apply {
                waitingFunction?.invoke()
                releaseJob()
                reset()
                mContext?.get()?.let {
                    if (isLocal) {
                        val assetFileDescriptor = it.assets.openFd(url)
                        assetFileDescriptor?.let { afd ->
                            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        }
                    } else {
                        setDataSource(url)
                    }
                }
                prepareAsync()
                setOnPreparedListener { it ->
                    audioFocus()
                    it.start()
                    progressCallBack?.let { startProgressJob(it) }
                    startPlayFunction?.invoke()
                }
                setOnCompletionListener {
                    playFinishFunction?.invoke()
                    manager?.abandonAudioFocus(audioFocusListener)
                }
                setOnErrorListener { mp, what, extra ->
                    errorCallBack?.invoke()
                    true
                }
            }
        }
    }

    /**
     * 开启播放进度任务
     */
    private fun startProgressJob(progressCallBack: IntListener) {
        mJob = GlobalScope.launch {
            withContext(Dispatchers.IO) {
                mMediaPlayer?.let {
                    while (it.isPlaying) {
                        val currentPosition = it.currentPosition
                        val duration = it.duration
                        progressCallBack?.invoke((currentPosition.toFloat() / duration.toFloat() * 100).toInt())
                        delay(1000)
                    }
                }
            }
        }
    }

    /**
     * 暂停 播放
     */
    fun playPause() {
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                releaseJob()
            }
        }
    }

    /**
     * 恢复播放
     */
    fun playResume() {
        mMediaPlayer?.let {
            if (it.isPlaying.not()) {
                it.start()
                startProgressJob(mProgressCallBack)
            }
        }

    }

    /**
     * 设置播放进度
     * @param progress  当前进度 eg:30
     */
    fun seekTo(progress: Int) {
        mMediaPlayer?.let {
            it.seekTo(it.duration * progress / 100)
        }
    }


    /**
     * 是否正在播放
     */
    fun isPlaying() = mMediaPlayer?.isPlaying ?: false

    override fun clear(context: Context?) {
        releaseJob()
        manager?.abandonAudioFocus(audioFocusListener)
        mMediaPlayer?.let {
            it?.release()
            mMediaPlayer = null
        }

    }

    private fun releaseJob() {
        mJob?.let {
            it.cancel()
            mJob = null
        }
    }

    private fun audioFocus() {
        manager?.requestAudioFocus(
            audioFocusListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

    }

    private val audioFocusListener =
        OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    LogUtil.i(msg = "永久性失去音频焦点")
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    LogUtil.i(msg = "暂时失去音频焦点")
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    LogUtil.i(msg = "暂时失去音频焦点，应用应该降低音量")
                }

                AudioManager.AUDIOFOCUS_GAIN -> {
                    LogUtil.i(msg = "重新获取音频焦点")
                }
            }
        }

}

//这是一个可空的带一个Int参数且返回 Unit 的函数类型。
typealias IntListener = ((Int) -> Unit)?