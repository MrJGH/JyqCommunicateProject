package com.jxw.basemodel.bean

/**
 *
 * @author: jyq
 * @date: 2024/1/3
 * @descp: 联系人列表
 */
data class UserListModel(
    var userName:String="",
    var userId:String="",
    var weixinUnionid:String=""
){
    fun  getConversationID():String{
        return "c2c_user_$weixinUnionid"
    }
}
