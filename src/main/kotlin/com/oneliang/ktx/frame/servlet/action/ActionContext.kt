package com.oneliang.ktx.frame.servlet.action

import java.util.ArrayList
import kotlin.collections.Map.Entry
import java.util.concurrent.ConcurrentHashMap

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList

import com.oneliang.exception.InitializeException
import com.oneliang.frame.AbstractContext
import com.oneliang.util.common.JavaXmlUtil
import com.oneliang.util.common.StringUtil

open class ActionContext : AbstractContext() {

    /**
     * @return the globalExceptionForwardBean
     */
    val globalExceptionForwardPath: String?
        get() {
            var path: String? = null
            if (globalExceptionForwardBean != null) {
                path = globalExceptionForwardBean.path
            }
            return path
        }

    /**
     * initialize
     * @param parameters
     */
    open fun initialize(parameters: String) {
        try {
            var tempClassesRealPath = classesRealPath
            if (tempClassesRealPath == null) {
                tempClassesRealPath = this.classLoader.getResource(StringUtil.BLANK).getPath()
            }
            val path = tempClassesRealPath!! + parameters
            val document = JavaXmlUtil.parse(path)
            if (document != null) {
                val root = document!!.getDocumentElement()
                //global forward list
                val globalForwardElementList = root.getElementsByTagName(GlobalForwardBean.TAG_GLOBAL_FORWARD)
                if (globalForwardElementList != null) {
                    val length = globalForwardElementList!!.getLength()
                    for (index in 0 until length) {
                        val globalForwardBean = GlobalForwardBean()
                        val globalForward = globalForwardElementList!!.item(index)
                        val attributeMap = globalForward.getAttributes()
                        JavaXmlUtil.initializeFromAttributeMap(globalForwardBean, attributeMap)
                        val globalForwardBeanName = globalForwardBean.name
                        globalForwardBeanMap[globalForwardBeanName] = globalForwardBean
                        globalForwardMap[globalForwardBeanName] = globalForwardBean.path
                    }
                }
                //global exception forward
                val globalExceptionForwardElementList = root.getElementsByTagName(GlobalExceptionForwardBean.TAG_GLOBAL_EXCEPTION_FORWARD)
                if (globalExceptionForwardElementList != null && globalExceptionForwardElementList!!.getLength() > 0) {
                    val attributeMap = globalExceptionForwardElementList!!.item(0).getAttributes()
                    JavaXmlUtil.initializeFromAttributeMap(globalExceptionForwardBean, attributeMap)
                }
                //action list
                val actionElementList = root.getElementsByTagName(ActionBean.TAG_ACTION)
                //xml to object
                if (actionElementList != null) {
                    val length = actionElementList!!.getLength()
                    for (index in 0 until length) {
                        val actionElement = actionElementList!!.item(index)
                        //action bean
                        val actionBean = ActionBean()
                        val attributeMap = actionElement.getAttributes()
                        JavaXmlUtil.initializeFromAttributeMap(actionBean, attributeMap)
                        //node list
                        val childNodeElementList = actionElement.getChildNodes()
                        if (childNodeElementList != null) {
                            val childNodeLength = childNodeElementList!!.getLength()
                            for (nodeIndex in 0 until childNodeLength) {
                                val childNodeElement = childNodeElementList!!.item(nodeIndex)
                                val childNodeElementName = childNodeElement.getNodeName()
                                //interceptorList
                                if (childNodeElementName == ActionInterceptorBean.TAG_INTERCEPTOR) {
                                    val actionInterceptorBean = ActionInterceptorBean()
                                    val interceptorAttributeMap = childNodeElement.getAttributes()
                                    JavaXmlUtil.initializeFromAttributeMap(actionInterceptorBean, interceptorAttributeMap)
                                    actionBean.addActionBeanInterceptor(actionInterceptorBean)
                                } else if (childNodeElementName == ActionForwardBean.TAG_FORWARD) {
                                    val actionForwardBean = ActionForwardBean()
                                    val forwardAttributeMap = childNodeElement.getAttributes()
                                    JavaXmlUtil.initializeFromAttributeMap(actionForwardBean, forwardAttributeMap)
                                    actionBean.addActionForwardBean(actionForwardBean)
                                }//forwardList
                            }
                        }
                        var actionInstance: ActionInterface? = null
                        if (!objectMap.containsKey(actionBean.id)) {
                            actionInstance = this.classLoader.loadClass(actionBean.type).newInstance()
                            objectMap.put(actionBean.id, actionInstance)
                        } else {
                            actionInstance = objectMap.get(actionBean.id)
                        }
                        actionBean.actionInstance = actionInstance
                        actionBeanMap[actionBean.id] = actionBean
                        var actionBeanList: MutableList<ActionBean>? = null
                        if (pathActionBeanMap.containsKey(actionBean.path)) {
                            actionBeanList = pathActionBeanMap[actionBean.path]
                        } else {
                            actionBeanList = ArrayList()
                            pathActionBeanMap[actionBean.path] = actionBeanList
                        }
                        actionBeanList!!.add(actionBean)
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
        actionBeanMap.clear()
        pathActionBeanMap.clear()
        globalForwardBeanMap.clear()
        globalForwardMap.clear()
    }

    /**
     * interceptor inject
     */
    fun interceptorInject() {
        val iterator = actionBeanMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val actionBean = entry.value
            val actionInterceptorBeanList = actionBean.actionInterceptorBeanList
            for (actionInterceptorBean in actionInterceptorBeanList) {
                if (objectMap.containsKey(actionInterceptorBean.id)) {
                    val interceptorInstance = objectMap.get(actionInterceptorBean.id) as Interceptor
                    actionInterceptorBean.interceptorInstance = interceptorInstance
                }
            }
        }
    }

    /**
     * find action bean
     * @param path
     * @return List<ActionBean>
    </ActionBean> */
    fun findActionBeanList(path: String): List<ActionBean>? {
        var actionBeanList: List<ActionBean>? = null
        if (pathActionBeanMap.containsKey(path)) {
            actionBeanList = pathActionBeanMap[path]
        }
        return actionBeanList
    }

    /**
     * find action
     * @param beanId
     * @return action object
     */
    fun findAction(beanId: String): ActionInterface? {
        var action: ActionInterface? = null
        if (objectMap.containsKey(beanId)) {
            action = objectMap.get(beanId)
        }
        return action
    }

    /**
     * find global forward
     * @param name forward name
     * @return forward path
     */
    fun findGlobalForwardPath(name: String?): String? {
        var path: String? = null
        if (name != null) {
            path = globalForwardMap[name]
        }
        return path
    }

    companion object {

        protected val actionBeanMap: MutableMap<String, ActionBean> = ConcurrentHashMap()
        protected val pathActionBeanMap: MutableMap<String, List<ActionBean>> = ConcurrentHashMap()
        protected val globalForwardBeanMap: MutableMap<String, GlobalForwardBean> = ConcurrentHashMap()
        protected val globalForwardMap: MutableMap<String, String> = ConcurrentHashMap()
        protected val globalExceptionForwardBean = GlobalExceptionForwardBean()
    }
}
