package com.jxw.basemodel.network


data class Response<T>(
        var code: Int = -1,
        var msg: String = "",
        var data: T? = null,
        var baseUrl: String? = null,
)

