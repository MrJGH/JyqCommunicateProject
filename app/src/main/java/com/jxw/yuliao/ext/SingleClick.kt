package com.jxw.yuliao.ext

import android.view.View
import com.jxw.yuliao.R


private var <T : View>T.triggerLastTime: Long
    get() = if (getTag(R.id.triggerLastTimeKey) != null) getTag(R.id.triggerLastTimeKey) as Long else 0
    set(value) {
        setTag(R.id.triggerLastTimeKey, value)
    }

private var <T : View> T.triggerDelay: Long
    get() = if (getTag(R.id.triggerDelayKey) != null) getTag(R.id.triggerDelayKey) as Long else -1
    set(value) {
        setTag(R.id.triggerDelayKey, value)
    }


private fun <T : View> T.clickEnable(): Boolean {
    var clickable = false
    val currentClickTime = System.currentTimeMillis()
    if (currentClickTime - triggerLastTime >= triggerDelay) {
        clickable = true
    }
    triggerLastTime = currentClickTime
    return clickable
}


/***
 * 带延迟过滤点击事件的 View 扩展
 * @param delay Long 延迟时间，默认1500毫秒
 * @return Unit
 */
fun <T : View> T.singClick(delay: Long = 500, block: (T) -> Unit) {
    triggerDelay = delay
    setOnClickListener {
        if (clickEnable()) {
            block(this)
        }
    }
}
/***
 * 带延迟过滤点击事件的 View 扩展 并且触摸的时候出发点击
 * @param delay Long 延迟时间，默认1000毫秒
 * @return Unit
 */
fun <T : View> T.singClick2(delay: Long = 1000, block: (T) -> Unit) {
    triggerDelay = delay
    setOnClickListener {
        if (clickEnable()) {
            requestAllFocus()
            touchPerformClick()
            block(this)
        }
    }
}

/**
 * 应用场景  处理带有默认选中的 一个view,同时点击其他view 的时候 控制焦点到其他view 上，同时被选中，并且移除当前view 上的焦点
 * 需配合selector 使用 ，selector中要存在state_focused和state_select属性
 * 原理是通过 isSelected控制默认选中和移除焦点可参考player_type_selector
 * @param delay  Long 延迟时间，默认1000毫秒
 * @param view 即将失去焦点的view
 * @param T  获取焦点的view
 */
fun  <T : View> T.singleClick3(delay: Long = 1000, view: View, block: () -> Unit) {
    singClick2(delay) {
        block()
        view.isSelected = false
    }
    setOnFocusChangeListener { v, hasFocus ->
        if (hasFocus) {
            view.isSelected = false
        }
    }
}