package com.jxw.basemodel.base

import com.jxw.basemodel.bean.SelectHeaderModel


/**
 *
 * @author: jyq
 * @date: 2023/12/22
 * @descp:  typalias 统一管理
 */
//这是一个可空的不带参数且返回 Unit 的函数类型。
typealias  EmptyListener = (() -> Unit)?
//通用只有一个String
typealias  CommonStringListener = ((String) -> Unit)?

//选择头像
typealias  SelectHeaderListener = ((SelectHeaderModel) -> Unit)?

typealias ErrorListener = ((Throwable) -> Unit)?

//网络请求回调
typealias  ErrorListener2 = ((Int, String) -> Unit)?

typealias ActivateErrorListener = ((String,String) -> Unit)?
