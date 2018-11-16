package com.oneliang.ktx.frame.ioc.aop

import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

class DataCacheInvokeProcessor : InvokeProcessor {

    @Throws(Throwable::class)
    override fun invoke(`object`: Any, method: Method, args: Array<Any>?): Any? {
        var returnValue: Any? = null
        val objectMethod = `object`.javaClass.getMethod(method.name, *method.parameterTypes)
        if (objectMethod.isAnnotationPresent(DataCache::class.java) && (args == null || args.size == 0)) {
            if (methodUpdateInvokeMap.containsKey(objectMethod)) {
                returnValue = methodUpdateInvokeMap[objectMethod].dataCache
            } else {
                synchronized(method) {
                    if (methodUpdateInvokeMap.containsKey(objectMethod)) {
                        returnValue = methodUpdateInvokeMap[objectMethod].dataCache
                    } else {
                        val dataCache = objectMethod.getAnnotation(DataCache::class.java)
                        returnValue = method.invoke(`object`, *args)
                        val methodUpdateInvoke = MethodUpdateInvoke()
                        methodUpdateInvoke.method = method
                        methodUpdateInvoke.interfaceImpl = `object`
                        methodUpdateInvoke.dataCache = returnValue
                        methodUpdateInvoke.updateTime = dataCache.updateTime()
                        methodUpdateInvokeMap[objectMethod] = methodUpdateInvoke
                        val thread = Thread(methodUpdateInvoke)
                        threadMethodMap[thread] = objectMethod
                        thread.start()
                    }
                }
            }
        } else if (objectMethod.isAnnotationPresent(DataCacheUpdate::class.java)) {
            val dataCacheUpdate = objectMethod.getAnnotation(DataCacheUpdate::class.java)
            val dataCacheMethodName = dataCacheUpdate.dataCacheMethod()
            val dataCacheMethod = `object`.javaClass.getMethod(dataCacheMethodName)
            if (methodUpdateInvokeMap.containsKey(dataCacheMethod)) {
                val dataCache = methodUpdateInvokeMap[dataCacheMethod].dataCache
                if (args != null && args.size > 0) {
                    args[args.size - 1] = dataCache
                }
            }
            returnValue = method.invoke(`object`, *args)
        } else {
            returnValue = method.invoke(`object`, *args)
        }
        return returnValue
    }

    class MethodUpdateInvoke : Runnable {
        internal var method: Method? = null
        internal var interfaceImpl: Any? = null
        internal var dataCache: Any? = null
        internal var updateTime: Long = 10000
        override fun run() {
            while (true) {
                try {
                    if (this.method != null && this.interfaceImpl != null) {
                        Thread.sleep(this.updateTime)
                        this.dataCache = this.method!!.invoke(this.interfaceImpl)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    companion object {

        private val threadMethodMap = ConcurrentHashMap<Thread, Method>()
        private val methodUpdateInvokeMap = ConcurrentHashMap<Method, MethodUpdateInvoke>()
    }
}
