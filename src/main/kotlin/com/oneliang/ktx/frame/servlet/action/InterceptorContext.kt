package com.oneliang.ktx.frame.servlet.action

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList

import com.oneliang.exception.InitializeException
import com.oneliang.frame.AbstractContext
import com.oneliang.util.common.JavaXmlUtil
import com.oneliang.util.common.StringUtil

open class InterceptorContext : AbstractContext() {

    /**
     * initialize
     */
    open fun initialize(parameters: String) {
        try {
            var path = parameters
            var tempClassesRealPath = classesRealPath
            if (tempClassesRealPath == null) {
                tempClassesRealPath = this.classLoader.getResource(StringUtil.BLANK).getPath()
            }
            path = tempClassesRealPath!! + path
            val document = JavaXmlUtil.parse(path)
            if (document != null) {
                val root = document!!.getDocumentElement()
                //global interceptor list
                val globalInterceptorElementList = root.getElementsByTagName(GlobalInterceptorBean.TAG_GLOBAL_INTERCEPTOR)
                if (globalInterceptorElementList != null) {
                    val globalInterceptorLength = globalInterceptorElementList!!.getLength()
                    for (index in 0 until globalInterceptorLength) {
                        val globalInterceptorElement = globalInterceptorElementList!!.item(index)
                        val globalInterceptor = GlobalInterceptorBean()
                        val attributeMap = globalInterceptorElement.getAttributes()
                        JavaXmlUtil.initializeFromAttributeMap(globalInterceptor, attributeMap)
                        var interceptorInstance: Interceptor? = null
                        interceptorInstance = this.classLoader.loadClass(globalInterceptor.type).newInstance()
                        globalInterceptor.interceptorInstance = interceptorInstance
                        globalInterceptorBeanMap[globalInterceptor.id] = globalInterceptor
                        objectMap.put(globalInterceptor.id, interceptorInstance)
                        val mode = globalInterceptor.mode
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
                    val interceptorElementLength = interceptorElementList!!.getLength()
                    for (index in 0 until interceptorElementLength) {
                        val interceptorElement = interceptorElementList!!.item(index)
                        val interceptor = InterceptorBean()
                        val attributeMap = interceptorElement.getAttributes()
                        JavaXmlUtil.initializeFromAttributeMap(interceptor, attributeMap)
                        var interceptorInstance: Interceptor? = null
                        interceptorInstance = this.classLoader.loadClass(interceptor.type).newInstance()
                        interceptor.interceptorInstance = interceptorInstance
                        interceptorBeanMap[interceptor.id] = interceptor
                        objectMap.put(interceptor.id, interceptorInstance)
                    }
                }
            }
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }

    }

    /**
     * destroy
     */
    fun destroy() {
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

    companion object {

        protected val globalInterceptorBeanMap: MutableMap<String, GlobalInterceptorBean> = ConcurrentHashMap()
        protected val interceptorBeanMap: MutableMap<String, InterceptorBean> = ConcurrentHashMap()
        protected val beforeGlobalInterceptorList: MutableList<Interceptor> = CopyOnWriteArrayList()
        protected val afterGlobalInterceptorList: MutableList<Interceptor> = CopyOnWriteArrayList()
    }
}
