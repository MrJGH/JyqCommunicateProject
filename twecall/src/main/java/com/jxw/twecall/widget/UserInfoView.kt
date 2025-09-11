package com.jxw.twecall.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.jxw.twecall.R
import com.jxw.twecall.manager.TweCallStateManager
import com.jxw.twecall.model.CallMediaType
import com.jxw.twecall.model.CallRole
import com.jxw.twecall.model.CallStatus
import com.jxw.twecall.ui.TweCallActivity
import com.jxw.twecall.utils.ext.gone
import com.jxw.twecall.utils.ext.visible

/**
 *
 * @author: jyq
 * @desc: 用户信息view
 * @date: 2024/11/19
 */
class UserInfoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private lateinit var mTvWaitTag: AppCompatTextView
    private lateinit var mIvUserHeader: RoundCornerImageView  //头像
    private lateinit var mTvUserName: AppCompatTextView  //姓名

    private var callStatusObserver = Observer<CallStatus> {
        when (it) {
            CallStatus.Waiting -> {
                mTvWaitTag.visible()
                mTvWaitTag.text = getWaitTag()
            }

            CallStatus.AudioAccept, CallStatus.VideoAccept -> {
                if (TweCallStateManager.mediaType.value == CallMediaType.Video) {
                    gone()
                } else {
                    mTvWaitTag.gone()
                }
            }

            CallStatus.HangUp -> {
                mTvWaitTag.gone()
            }

            else -> {}
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_user_info_view, this)
        initView()
        addObserver()
    }

    private fun initView() {
        mIvUserHeader = findViewById(R.id.iv_header)
        Glide.with(context).load(TweCallActivity.userHeader).into(mIvUserHeader)

        mTvUserName = findViewById(R.id.tv_name)
        mTvUserName.text = TweCallActivity.userName

        mTvWaitTag = findViewById(R.id.tv_tag)
        mTvWaitTag.text = getWaitTag()

    }

    private fun addObserver() {
        TweCallStateManager.callStatus.observeForever(callStatusObserver)
    }

    fun removeObserver() {
        TweCallStateManager.callStatus.removeObserver(callStatusObserver)
    }

    /**
     * 获取 等待提示 文案
     */
    private fun getWaitTag(): String {
        return if (TweCallStateManager.callStatus.value == CallStatus.Waiting) {
            when {
                TweCallStateManager.callRole.value == CallRole.Caller -> {
                    context.getString(R.string.waiting_accept)
                }

                TweCallStateManager.callRole.value == CallRole.Called && TweCallStateManager.mediaType.value == CallMediaType.Audio -> {
                    context.getString(R.string.invite_audio_call)
                }

                TweCallStateManager.callRole.value == CallRole.Called && TweCallStateManager.mediaType.value == CallMediaType.Video -> {
                    context.getString(R.string.invite_video_call)
                }

                else -> ""
            }
        } else ""
    }
}