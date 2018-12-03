package com.oneliang.ktx.frame.ioc.aop

import java.lang.reflect.Method

interface AfterThrowingProcessor {

    /**
     * after throwing
     * @param instance
     * @param method
     * @param args
     * @param exception
     */
    fun afterThrowing(instance: Any, method: Method, args: Array<Any>, exception: Exception)
}
