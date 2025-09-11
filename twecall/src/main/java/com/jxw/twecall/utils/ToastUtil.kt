package com.jxw.twecall.utils

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.jxw.twecall.R
import com.jxw.twecall.TweCallManager

object ToastUtil {

    fun showTips(msg: String) {
        showTipsImpl(msg, Toast.LENGTH_SHORT)
    }

    fun showTips(msg: String, duration: Int) {
        showTipsImpl(msg, duration)
    }

    private fun showTipsImpl(msg: String, duration: Int) {
        try {
            val mContentView: View = LayoutInflater.from(TweCallManager.mContext?.get())
                .inflate(R.layout.widget_tips_toast, null, false)

            val toastTxt = mContentView?.findViewById<TextView>(R.id.tip_toast_txt)
            val toast = Toast(TweCallManager.mContext?.get())
            toast.view = mContentView
            toastTxt?.text = msg
            toast.setGravity(Gravity.TOP, 0, TweCallManager.mContext?.get()?.resources?.getDimensionPixelOffset(R.dimen.y417)?:0)
            toast.duration = duration
            toast.show()
        } catch (e: Exception) {
            LogUtil.e(msg= "show tips error$e")
        }
    }
}