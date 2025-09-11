package com.jxw.twecall.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.jxw.twecall.R
import com.jxw.twecall.TweCallManager
import com.jxw.twecall.manager.TweCallStateManager
import com.jxw.twecall.ui.TweCallActivity
import com.jxw.twecall.utils.ext.gone
import com.jxw.twecall.utils.ext.visible
import com.jxw.twecall.widget.MoveTextureViewHelper.viewTranslationX
import com.jxw.twecall.widget.MoveTextureViewHelper.viewTranslationY

/**
 *
 * @author: jyq
 * @desc: 可移动的TextureView
 * @date: 2024/11/8
 */
class MoveTextureView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var lastX: Float = 0f
    private var lastY: Float = 0f

    private var isDragging: Boolean = false
    private val touchSlop = 10 // 定义一个阈值，单位为像素
    private var isFullScreenView = false //当前是否是全屏
    private lateinit var mTextureView: TextureView
    private lateinit var mClHeader: ConstraintLayout
    private lateinit var mIvHeader: AppCompatImageView

    private var mTextureViewType: Int = 0   //textureView 类型
    private var mTextureViewCanMove: Boolean = true   //textureView 是否可以移动

    private val isCameraStateObserver = Observer<Boolean> {
        if (it) {
            mClHeader.gone()
        } else {
            mClHeader.visible()
        }
    }
    private val wxCameraStatusObserver = Observer<Boolean> {
        if (it) {
            mClHeader.gone()
        } else {
            mClHeader.visible()
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_move_texture_view, this)
        if (attrs != null) {
            val array = context.obtainStyledAttributes(attrs, R.styleable.MoveTextureView)
            mTextureViewType = array.getInteger(R.styleable.MoveTextureView_textureView_type, 0)
            mTextureViewCanMove = array.getBoolean(R.styleable.MoveTextureView_textureView_can_move, true);
            array.recycle()
        }
        initView()
        addObserver()

    }

    private fun initView() {
        mTextureView = findViewById(R.id.texture_view)
        mClHeader = findViewById(R.id.cl_header)
        mIvHeader = findViewById(R.id.iv_header)
        Glide.with(context).load(TweCallActivity.userHeader).into(mIvHeader)
    }

    fun addObserver() {
        when (mTextureViewType) {
            0 -> TweCallStateManager.isCameraState.observeForever(isCameraStateObserver)
            1 -> TweCallStateManager.isWxCameraState.observeForever(wxCameraStatusObserver)
        }
    }

    fun removeObserver() {
        when (mTextureViewType) {
            0 -> TweCallStateManager.isCameraState.removeObserver(isCameraStateObserver)
            1 -> TweCallStateManager.isWxCameraState.removeObserver(wxCameraStatusObserver)
        }
    }

    fun getTextTureView() = mTextureView

    /**
     * 初始化偏移量
     */
    fun initTranslation() {
        this.translationX = viewTranslationX.toFloat()
        this.translationY = viewTranslationY.toFloat()
    }

    /**
     * 设置画面
     */
    fun changeSmallView(
        textureViewWidth: Int = context.resources.getDimensionPixelSize(R.dimen.x140),
        textureViewHeight: Int = context.resources.getDimensionPixelSize(R.dimen.y230),
        headerWidth: Int = context.resources.getDimensionPixelSize(R.dimen.x60),
        headerHeight: Int = context.resources.getDimensionPixelSize(R.dimen.x60),
    ) {
        isFullScreenView = false
        updateLayoutParams<ViewGroup.MarginLayoutParams> {
            width = textureViewWidth
            height = textureViewHeight
            topMargin = 0
            leftMargin = 0
        }
        this.translationX = viewTranslationX.toFloat()
        this.translationY = viewTranslationY.toFloat()

        mIvHeader.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            width = headerWidth
            height = headerHeight
            topMargin = context.resources.getDimensionPixelSize(R.dimen.x87)
            leftMargin = 0
        }
        bringToFront()
    }

    /**
     * 设置全屏画面
     */
    fun changeFullView() {
        isFullScreenView = true
        updateLayoutParams<ViewGroup.MarginLayoutParams> {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
            topMargin = -viewTranslationY
            leftMargin = -viewTranslationX
        }
        mIvHeader.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            width = context.resources.getDimensionPixelSize(R.dimen.x140)
            height = context.resources.getDimensionPixelSize(R.dimen.x140)
            topMargin = context.resources.getDimensionPixelSize(R.dimen.x118)
            leftMargin = -viewTranslationX
        }
    }

    var rawX: Float = 0f
    var rawY: Float = 0f
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (mTextureViewCanMove) {
            event?.let {
                when (it.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        rawX = it.rawX
                        rawY = it.rawY
                        lastX = it.rawX - this.translationX
                        lastY = it.rawY - this.translationY
                        isDragging = false
//                        Log.e("", "ACTION_DOWN lastX = " + lastX + "   lastY = " + lastY)
//                        Log.e("", "ACTION_DOWN rawX = " + it.rawX + "rawY = " + it.rawY)
//                        Log.e("", "ACTION_DOWN x = " + it.x + "y = " + it.y)
                    }

                    MotionEvent.ACTION_MOVE -> {
//                        Log.e("", "ACTION_MOVE")
                        if (!isDragging) {
                            // 计算拖动距离
                            val dx = it.rawX - rawX
                            val dy = it.rawY - rawY
//                            Log.e("", "rawX = " + it.rawX + "rawY = " + it.rawY)
//                            Log.e("", "dx = " + dx + "dy = " + dy)
                            if (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop) {
                                isDragging = true // 超过阈值，标记为拖动
                            }
                        }
                        if (isDragging && !isFullScreenView) {
                            var parentViewGroup = parent as? ViewGroup
                            val parentWidth = parentViewGroup?.width?.toFloat() ?: this.width.toFloat()
                            val parentHeight = parentViewGroup?.height?.toFloat() ?: this.height.toFloat()
                            val viewWidth = this.width.toFloat()
                            val viewHeight = this.height.toFloat()

                            // 计算新的位置，但不超过边界
                            val newX = 0f.coerceAtLeast((it.rawX - lastX).coerceAtMost(parentWidth - viewWidth))
                            val newY = 0f.coerceAtLeast((it.rawY - lastY).coerceAtMost(parentHeight - viewHeight))
//                            Log.e("", "newX = " + newX + "newY = " + newY)
                            // 更新TextureView的位置
                            this.translationX = newX
                            this.translationY = newY

                            viewTranslationX = newX.toInt()
                            viewTranslationY = newY.toInt()
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            // 如果没有拖动，执行点击事件
                            performClick()
                        }
                        isDragging = false
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        isDragging = false
                    }
                }
                return true // 消耗事件，防止其他视图接收到触摸事件
            }
            return false
        } else {
            return super.onTouchEvent(event)
        }
    }
}

object MoveTextureViewHelper {
    private val defaultTranslationX: Int = TweCallManager.mContext?.get()?.resources?.getDimensionPixelSize(R.dimen.x320) ?: 0  //默认X偏移量
    private val defaultTranslationY: Int = TweCallManager.mContext?.get()?.resources?.getDimensionPixelSize(R.dimen.y20) ?: 0  //默认Y偏移量
    var viewTranslationX: Int = 0 + defaultTranslationX;//x全局的横向偏移量
    var viewTranslationY: Int = 0 + defaultTranslationY//y全局的纵向偏移量

    /**
     * 重置MoveTextureView  全局的偏移量
     * 一般当前界面销毁时候调用
     */
    fun resetViewTranslation() {
        viewTranslationX = defaultTranslationX
        viewTranslationY = defaultTranslationY
    }

}