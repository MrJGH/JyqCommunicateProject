package com.jxw.yuliao.activity

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.jxw.yuliao.R
import com.jxw.basemodel.base.CommonStringListener
import com.jxw.basemodel.base.EmptyListener
import com.jxw.yuliao.databinding.FragmentEditNameBinding
import com.jxw.yuliao.ext.enable
import com.jxw.yuliao.ext.singClick
import com.jxw.yuliao.utils.getStringFromResource
import com.jxw.yuliao.widget.EditTextDeleteView

/**
 *
 * @author: jyq
 * @date: 2023/11/20
 * @descp: 编辑名字弹窗
 */
class EditNameDialogFragment : DialogFragment(), EditTextDeleteView.TextChangeListener {
    private var mListener: CommonStringListener = null
    private lateinit var mBinding: FragmentEditNameBinding
    private var mInputName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_edit_name,
            container,
            false
        )
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            decorView.setBackgroundColor(Color.TRANSPARENT)
            setDimAmount(0f)
            val params: WindowManager.LayoutParams = attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            attributes = params

        }
        mBinding?.lifecycleOwner = this
        return mBinding?.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    private fun initListener() {
        mBinding.edtName.setTextChangeListener(this)
        mBinding.sureBtn.singClick {

            if (mInputName.isEmpty()) {
                Toast.makeText(
                    context,
                    getStringFromResource(R.string.input_name_can_not_empty),
                    Toast.LENGTH_SHORT
                ).show()
                return@singClick
            } else {
                dismiss()
                mListener?.invoke(mInputName)
            }
        }
        mBinding.cancelBtn.singClick { dismiss() }
    }

    fun setSureListener(listener: CommonStringListener) {
        this.mListener = listener
    }

    override fun setTextChangeFinish(string: String) {
        this.mInputName = string
    }

    override fun setTextChange(isEnable: Boolean) {
        mBinding.sureBtn.enable(isEnable)
    }
}