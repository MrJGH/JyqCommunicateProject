package com.jxw.twecall.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import com.jxw.twecall.manager.TweCallStateManager
import com.jxw.twecall.model.CallMediaType
import com.jxw.twecall.model.CallStatus
import com.jxw.twecall.utils.ext.gone
import com.jxw.twecall.utils.ext.visible
import com.jxw.twecall.utils.formatSecondsTo00
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *
 * @author: jyq
 * @desc: 通讯时间 view
 * @date: 2024/11/21
 */
class CallTimerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var mTimeCount = 0

    private var callStatusObserver = Observer<CallStatus> {
        when {
            TweCallStateManager.mediaType.value == CallMediaType.Audio && it == CallStatus.AudioAccept -> {
                visible()
                startTime()
            }

            TweCallStateManager.mediaType.value == CallMediaType.Video && it == CallStatus.VideoAccept -> {
                visible()
                startTime()
            }

            it == CallStatus.HangUp -> {
                stopTime()
                gone()
            }
        }
    }

    init {
        addObserver()
    }

    private fun addObserver() {
        TweCallStateManager.callStatus.observeForever(callStatusObserver)
    }

    fun removeObserver() {
        TweCallStateManager.callStatus.removeObserver(callStatusObserver)
    }

    private fun startTime() {
        coroutineScope.launch {
            while (isActive) {
                delay(1000)
                mTimeCount += 1
                withContext(Dispatchers.Main) {
                    text = mTimeCount.formatSecondsTo00()
                }
                TweCallStateManager.callTimeLiveData.postValue(mTimeCount)
            }
        }
    }

    private fun stopTime() {
        mTimeCount = 0
        coroutineScope.cancel()
    }

}