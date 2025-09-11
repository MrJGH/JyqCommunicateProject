package com.jxw.twecall.base

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.jxw.twecall.utils.TimeUtil

/**
 *
 * @author: jyq
 * @desc:
 * @date: 2024/11/21
 */
abstract class BaseCallView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    val timeUtil by lazy { TimeUtil() }

    init {
        LayoutInflater.from(context).inflate(getLayoutId(), this)
        initView()
        initListener()
        initData()
    }

    abstract fun getLayoutId(): Int
    abstract fun initView()

    open fun initListener() {}
    open fun initData() {}

    open fun addObserver() {}
    open fun removeObserver() {}

    fun clear() = removeObserver()


}