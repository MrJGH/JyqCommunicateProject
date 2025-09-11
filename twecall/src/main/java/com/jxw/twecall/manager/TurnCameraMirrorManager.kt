package com.jxw.twecall.manager

import com.jxw.basemodel.utils.LauncherDataUtil
import com.jxw.twecall.mirror.ITurnCameraMirror
import com.jxw.twecall.mirror.TurnCameraMirrorType1
import com.jxw.twecall.mirror.TurnCameraMirrorType2

/**
 *
 * @author: jyq
 * @desc: 镜像翻转配置 管理
 * @date: 2025/4/9
 */
internal class TurnCameraMirrorManager {

    private var mTurnCameraMirror: ITurnCameraMirror? = null


    fun turnCameraMirror(cameraMirrorType: Int = LauncherDataUtil.getCameraMirrorType()) {
        mTurnCameraMirror = when (cameraMirrorType) {
            1 -> TurnCameraMirrorType2()
            else -> TurnCameraMirrorType1()
        }
        mTurnCameraMirror?.turnCameraMirror()
    }
}