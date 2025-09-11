package com.jxw.twecall.listener

interface OnEncodeListener {
    fun onAudioEncoded(datas: ByteArray, pts: Long, seq: Long)
    fun onVideoEncoded(datas: ByteArray, pts: Long, seq: Long, isKeyFrame: Boolean)
}