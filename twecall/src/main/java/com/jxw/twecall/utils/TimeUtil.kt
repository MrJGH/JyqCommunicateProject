package com.jxw.twecall.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * @author: jyq
 * @desc:  倒计时器
 * @date: 2024/12/24
 */
class TimeUtil {
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var mCountTimeJob: Job? = null

    fun startCountdown(duration: Long = 60000, onFinish: () -> Unit) {
        mCountTimeJob = coroutineScope.launch {
            var remainingTime = duration
            while (remainingTime > 0 && isActive) {
                LogUtil.e(msg = "" + remainingTime)
                delay(1000L)
                remainingTime -= 1000L
            }
            // 倒计时结束，执行回调
            onFinish()
        }
    }

    fun stopCountdown() {
        mCountTimeJob?.cancel()
    }
}