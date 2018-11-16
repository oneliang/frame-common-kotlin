package com.oneliang.ktx.frame.ioc.aop

import java.lang.reflect.Method

interface BeforeInvokeProcessor {

    /**
     * before invoke
     * @param object
     * @param method
     * @param args
     * @throws Throwable
     */
    @Throws(Throwable::class)
    fun beforeInvoke(`object`: Any, method: Method, args: Array<Any>)
}
