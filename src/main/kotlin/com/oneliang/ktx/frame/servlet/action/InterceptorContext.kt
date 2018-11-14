package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.AbstractContext
import com.oneliang.ktx.util.common.JavaXmlUtil
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

open class InterceptorContext : AbstractContext() {

    companion object {

        internal val globalInterceptorBeanMap: MutableMap<String, GlobalInterceptorBean> = ConcurrentHashMap()
        internal val interceptorBeanMap: MutableMap<String, InterceptorBean> = ConcurrentHashMap()
        internal val beforeGlobalInterceptorList: MutableList<Interceptor> = CopyOnWriteArrayList()
        internal val afterGlobalInterceptorList: MutableList<Interceptor> = CopyOnWriteArrayList()
    }

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        try {
            var path = parameters
            var tempClassesRealPath = classesRealPath
            if (tempClassesRealPath == null) {
                tempClassesRealPath = this.classLoader.getResource(Constants.String.BLANK).getPath()
            }
            path = tempClassesRealPath!! + path
            val document = JavaXmlUtil.parse(path)
            val root = document.documentElement
            //global interceptor list
            val globalInterceptorElementList = root.getElementsByTagName(GlobalInterceptorBean.TAG_GLOBAL_INTERCEPTOR)
            if (globalInterceptorElementList != null) {
                val globalInterceptorLength = globalInterceptorElementList.length
                for (index in 0 until globalInterceptorLength) {
                    val globalInterceptorElement = globalInterceptorElementList.item(index)
                    val globalInterceptorBean = GlobalInterceptorBean()
                    val attributeMap = globalInterceptorElement.attributes
                    JavaXmlUtil.initializeFromAttributeMap(globalInterceptorBean, attributeMap)
                    val interceptorInstance: Interceptor = this.classLoader.loadClass(globalInterceptorBean.type).newInstance() as Interceptor
                    globalInterceptorBean.interceptorInstance = interceptorInstance
                    globalInterceptorBeanMap[globalInterceptorBean.id] = globalInterceptorBean
                    objectMap.put(globalInterceptorBean.id, interceptorInstance)
                    val mode = globalInterceptorBean.mode
                    if (mode != null) {
                        if (mode == GlobalInterceptorBean.INTERCEPTOR_MODE_BEFORE) {
                            beforeGlobalInterceptorList.add(interceptorInstance)
                        } else if (mode == GlobalInterceptorBean.INTERCEPTOR_MODE_AFTER) {
                            afterGlobalInterceptorList.add(interceptorInstance)
                        }
                    }
                }
            }
            //interceptor list
            val interceptorElementList = root.getElementsByTagName(InterceptorBean.TAG_INTERCEPTOR)
            if (interceptorElementList != null) {
                val interceptorElementLength = interceptorElementList.length
                for (index in 0 until interceptorElementLength) {
                    val interceptorElement = interceptorElementList.item(index)
                    val interceptor = InterceptorBean()
                    val attributeMap = interceptorElement.attributes
                    JavaXmlUtil.initializeFromAttributeMap(interceptor, attributeMap)
                    val interceptorInstance = this.classLoader.loadClass(interceptor.type).newInstance() as Interceptor
                    interceptor.interceptorInstance = interceptorInstance
                    interceptorBeanMap[interceptor.id] = interceptor
                    objectMap.put(interceptor.id, interceptorInstance)
                }
            }
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }

    }

    /**
     * destroy
     */
    override fun destroy() {
        globalInterceptorBeanMap.clear()
        interceptorBeanMap.clear()
        beforeGlobalInterceptorList.clear()
        afterGlobalInterceptorList.clear()
    }

    /**
     * @return the beforeGlobalInterceptorList
     */
    fun getBeforeGlobalInterceptorList(): List<Interceptor> {
        return beforeGlobalInterceptorList
    }

    /**
     * @return the afterGlobalInterceptorList
     */
    fun getAfterGlobalInterceptorList(): List<Interceptor> {
        return afterGlobalInterceptorList
    }
}
