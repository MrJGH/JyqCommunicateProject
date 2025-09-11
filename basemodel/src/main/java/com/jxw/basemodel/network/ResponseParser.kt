package com.jxw.basemodel.network

import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.exception.ParseException
import rxhttp.wrapper.parse.TypeParser
import rxhttp.wrapper.utils.convertTo
import java.io.IOException
import java.lang.reflect.Type


@Parser(name = "Response")
open class ResponseParser<T> : TypeParser<T> {

    //以下两个构造方法是必须的
    protected constructor() : super()
    constructor(type:Type) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: okhttp3.Response): T {
        //将okhttp3.Response转换为自定义的Response对象 这里的types就是自定义Response<T>里面的泛型类型*/
        val data: Response<T> = response.convertTo(Response::class, *types)
        val t = data.data                             //获取data字段
        if (data.code != 0 || t == null) { //这里假设code不等于200，代表数据不正确，抛出异常
            throw ParseException(data.code.toString(), data.msg, response)
        }
        return t
    }
}