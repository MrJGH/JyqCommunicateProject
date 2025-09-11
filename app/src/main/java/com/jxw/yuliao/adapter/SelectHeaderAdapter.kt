package com.jxw.yuliao.adapter

import android.content.Context
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.jxw.yuliao.R
import com.jxw.basemodel.bean.SelectHeaderModel

/**
 *
 * @author: jyq
 * @date: 2023/12/22
 * @descp: 选择头像adapter
 */
class SelectHeaderAdapter : BaseQuickAdapter<SelectHeaderModel, QuickViewHolder>() {
    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: SelectHeaderModel?
    ) {
        item?.drawableId?.let { holder.setImageResource(R.id.header_iv, it) }
        if (item?.isSelected == true) {
            holder.setVisible(R.id.header_select_iv, true)
        } else {
            holder.setVisible(R.id.header_select_iv, false)
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder = QuickViewHolder(R.layout.item_select_header_content, parent)
}