package com.jxw.twecall.manager

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.jxw.twecall.utils.LogUtil

/**
 *
 * @author: jyq
 * @date: 2024/5/18
 * @desc:生命周期管理
 */
object AppLifecycleObserverManager {
    var isForegroundTag = false

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleEventObserver { source, event ->
            LogUtil.i(msg = "process lifecycle $event ,$source")
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    //回到前台
                    isForegroundTag = true
                }

                Lifecycle.Event.ON_STOP -> {
                    //回到后台
                    isForegroundTag = false
                }

                else -> {}
            }
        })
    }

}
