package com.jxw.basemodel.base

import android.content.Context

/**
 *
 * @author: jyq
 * @date: 2023/12/16
 * @descp: manager  基类接口
 */
interface IBaseManager {
    fun  init(context: Context)
    fun  clear(context: Context?=null)
}