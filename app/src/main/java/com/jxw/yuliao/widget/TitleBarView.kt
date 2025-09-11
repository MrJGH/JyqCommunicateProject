package com.jxw.yuliao.widget

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.jxw.yuliao.R
import com.jxw.yuliao.ext.expandTouchArea
import com.jxw.yuliao.ext.singClick
import com.jxw.yuliao.utils.getDimensionFromResource

/**
 *
 * @author: jyq
 * @date: 2023/10/8
 * @descp: 自定义title
 */
class TitleBarView @JvmOverloads constructor(
    private var mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(mContext, attrs, defStyleAttr) {

    private var mViewLine: View? = null
    private var mIvRight: AppCompatImageView? = null
    private var mTiTle: AppCompatTextView? = null
    private var mIvBack: AppCompatImageView? = null
    private var mTvNext: AppCompatTextView? = null

    init {
        val mView =
            LayoutInflater.from(mContext).inflate(R.layout.layout_title_bar, this, true)

        attrs?.let {
            val a = mContext.obtainStyledAttributes(attrs, R.styleable.TitleBar)
            val title = a.getString(R.styleable.TitleBar_title)
            val focus = a.getBoolean(R.styleable.TitleBar_focusable, false)
            val isShowLine = a.getInteger(R.styleable.TitleBar_isShowLine, View.GONE)
            val imageRightVisibility = a.getInteger(R.styleable.TitleBar_imageRightVisibility, View.INVISIBLE)
            initView(mView)
            mViewLine?.visibility = isShowLine
            mIvRight?.visibility = imageRightVisibility
            mTiTle?.run {
                isFocusable = focus
                isSelected = true
                setTextColor(a.getColor(R.styleable.TitleBar_titleColor, Color.parseColor("#191D25")))
            }
            setBackgroundColor(a.getColor(R.styleable.TitleBar_titleBackgroundColor, Color.WHITE))
            setCenterTxt(title ?: "")
            a.recycle()
        }
        initListener()
    }

    private fun initView(view: View) {
        mViewLine = view.findViewById(R.id.line)
        mIvRight = view.findViewById<AppCompatImageView?>(R.id.right_iv).apply {
            expandTouchArea(getDimensionFromResource(R.dimen.x10))
        }
        mTiTle = view.findViewById(R.id.title)
        mIvBack = view.findViewById(R.id.back)
        mTvNext = view.findViewById(R.id.next_tv)

    }

    private fun initListener() {
        mIvBack?.setOnClickListener {
            if (mContext is Activity) {
                (mContext as Activity).finish()
            }
        }
    }

    fun getTextView(): TextView? {
        return mTiTle
    }

    fun getBackView(): AppCompatImageView? {
        return mIvBack
    }

    fun setCenterTxt(string: String?) {
        string?.let { mTiTle?.text = it }
    }

    fun setBackClickListener(listener: () -> Unit) {
        mIvBack?.setOnClickListener {
            listener()
        }
    }

    fun setRightImageClickListener(listener: () -> Unit) {
        mIvRight?.singClick {
            listener()
        }
    }

    fun setNextClickListener(listener: () -> Unit) {
        mTvNext?.singClick { listener() }
    }

    fun getNextTextView(): AppCompatTextView? = mTvNext


}