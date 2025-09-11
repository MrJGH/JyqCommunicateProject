package com.jxw.yuliao.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.jxw.yuliao.R
import com.jxw.yuliao.ext.gone
import com.jxw.yuliao.ext.visible
import com.jxw.yuliao.utils.dpToPx

/**
 * 带 删除图标的 view
 * (可以监听 文件输入，控制是否显示删除图标和 文字输入最大限制)
 */
class EditTextDeleteView constructor(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {
    private var drawable: Drawable? = null
    private var hint: String? = null
    private var imageMarginRight: Int? = null
    private var mInputType: Int = 1

    private var mTextChangeListener: TextChangeListener? = null
    private var mCommonEdt: TextView? = null
    private var mCommonHinTv: TextView? = null
    private var mCommonDeleteIv: AppCompatImageView? = null

    init {

        //获取xml布局文件中的自定义属性的值
        val array = context.obtainStyledAttributes(attrs, R.styleable.EditTextDeleteView)
        try {
            drawable = array.getDrawable(R.styleable.EditTextDeleteView_rightDrawable)
            hint = array.getText(R.styleable.EditTextDeleteView_hint) as String
            imageMarginRight = array.getDimension(
                R.styleable.EditTextDeleteView_imageMarginRight,
                dpToPx(16).toFloat()
            ).toInt()
            mInputType =
                array.getInt(R.styleable.EditTextDeleteView_inputType, InputType.TYPE_CLASS_TEXT)
        } catch (e: Exception) {
            Log.e("EditTextDeleteView", e.message.toString())
        } finally {
            //回收TypedArray
            array.recycle()
        }
        initView()
        initListener()
    }


    private fun initView() {
        //初始化组合控件布局
        var view = LayoutInflater.from(context).inflate(
            R.layout.common_edittext_del,
            this,
            true
        )

        mCommonEdt = view.findViewById(R.id.common_editText)
        mCommonHinTv = view.findViewById(R.id.common_hint_tv)
        mCommonDeleteIv = view.findViewById(R.id.common_dele_iv)

        //给控件赋值
        mCommonEdt?.hint = hint
        mCommonEdt?.inputType = mInputType
        if (drawable != null) {
            mCommonDeleteIv?.setImageDrawable(drawable)
        }
    }

    private fun initListener() {
        //给控件添加事件,点击删除图片的时候将EditText的文字内容设置为空字符
        mCommonDeleteIv?.setOnClickListener { mCommonEdt?.text = "" }
        //在EditText中的内容发生改变时,控制删除按钮的显示和隐藏
        mCommonEdt?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s.isNotEmpty()) {
                    mCommonDeleteIv?.visible()
                    if (s.length > 10) {
                        mCommonHinTv?.visible()
                        mTextChangeListener?.setTextChange(false)
                    } else {
                        mCommonHinTv?.gone()
                        mTextChangeListener?.setTextChange(true)
                    }
                } else {
                    mCommonDeleteIv?.gone()
                }
            }

            override fun afterTextChanged(s: Editable) {
                mTextChangeListener?.setTextChangeFinish(s.toString())
            }
        })
        mCommonEdt?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (mCommonEdt?.length()!! > 0) {
                    mCommonDeleteIv?.visible()
                } else {
                    mCommonDeleteIv?.gone()
                }
            } else {
                mCommonDeleteIv?.gone()
            }
        }

    }

    var text: String?
        get() = mCommonEdt?.text.toString()
        set(str) {
            mCommonEdt?.text = str
        }

    fun setTextChangeListener(textChangeListener: TextChangeListener) {
        mTextChangeListener = textChangeListener;
    }

    interface TextChangeListener {

        fun setTextChangeFinish(string: String)

        fun setTextChange(isEnable: Boolean)


    }

    fun setEditTextFocus(isFocus: Boolean, activity: AppCompatActivity) {
        mCommonEdt?.apply {
            isFocusableInTouchMode = isFocus
            isFocusable = isFocus
            requestFocus()
        }
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

}