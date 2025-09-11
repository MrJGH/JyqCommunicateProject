package com.jxw.basemodel.bean


/**
 *
 * @author: jyq
 * @date: 2023/12/22
 * @descp: 选择头像model
 */
data class SelectHeaderModel(
    var isSelected:Boolean =false,
    var id:Int = 0,
    var imageUrl:String,
    var drawableId:Int
)
