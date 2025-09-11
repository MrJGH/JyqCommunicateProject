package com.jxw.basemodel.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.jxw.basemodel.base.ErrorListener2
import com.jxw.basemodel.bean.ActivateInfoModel
import com.jxw.basemodel.bean.RegisterInfoModel
import com.jxw.basemodel.bean.UserInfoModel

/**
 *
 * @author: jyq
 * @desc:  java代码  调用接口 帮助类
 * @date: 2024/12/15
 */
object ApiHelper {
    /**
     * 获取好友WXOpenId
     */
    fun getCurrentUserOpenId(imUserId: String): LiveData<String> {
        return liveData {
            val openId = safeApiCall(
                callback = {},
                responseFunction = { DataRepository.getUserOpenId(imUserId) })
            openId?.let { emit(it) }
        }
    }

    /**
     * 获取 查询设备激活详情
     */
    fun getActivateInfo(): LiveData<ActivateInfoModel> {
        return liveData {
            val data = safeApiCall(
                callback = {},
                responseFunction = { DataRepository.getActivateInfo() }
            )
            data?.let { emit(it) }
        }
    }

    /**
     * 获取 用户信息 和 平台对应的SDK_APPID , SECRETKEY
     */
    fun getUseInfo(onError: ErrorListener2 = null): LiveData<RegisterInfoModel> {
        return liveData {
            val data = safeApiCall(
                callback = { onError { i, s -> onError?.invoke(i, s) } },
                responseFunction = { DataRepository.getUseInfo() }
            )
            data?.let { emit(it) }
        }
    }

    /**
     * 获取 绑定用户信息
     */
    fun getDeviceBindUserInfo(): LiveData<UserInfoModel> {
        return liveData {
            val data = safeApiCall(
                callback = {},
                responseFunction = { DataRepository.getDeviceBindUserInfo() })
            data?.let { emit(it) }
        }
    }

    /**
     * 保存用户信息
     */
    fun saveUserInfo(icon: String, nickname: String): LiveData<Any> {
        return liveData {
            val data = safeApiCall(
                callback = {},
                responseFunction = { DataRepository.saveUserInformation(icon = icon, nickname = nickname) })
            data?.let { emit(it) }
        }
    }


}