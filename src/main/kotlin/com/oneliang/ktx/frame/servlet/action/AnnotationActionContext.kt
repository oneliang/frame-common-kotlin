package com.oneliang.ktx.frame.servlet.action

import java.lang.reflect.Method
import java.util.ArrayList

import com.oneliang.Constants
import com.oneliang.exception.InitializeException
import com.oneliang.frame.AnnotationContextUtil
import com.oneliang.frame.servlet.action.Action.RequestMapping
import com.oneliang.frame.servlet.action.Action.RequestMapping.Interceptor
import com.oneliang.frame.servlet.action.Action.RequestMapping.Static
import com.oneliang.util.common.StringUtil

class AnnotationActionContext : ActionContext() {

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        try {
            val classList = AnnotationContextUtil.parseAnnotationContextParameter(parameters, classLoader, classesRealPath, jarClassLoader, projectRealPath, Action::class.java)
            if (classList != null) {
                for (clazz in classList!!) {
                    val classId = clazz.getName()
                    var actionInstance: Any? = null
                    if (!objectMap.containsKey(classId)) {
                        actionInstance = clazz.newInstance()
                        objectMap.put(classId, actionInstance)
                    } else {
                        actionInstance = objectMap.get(classId)
                    }
                    val methods = clazz.getMethods()
                    for (method in methods) {
                        if (method.isAnnotationPresent(RequestMapping::class.java)) {
                            val returnType = method.getReturnType()
                            if (returnType != null && returnType == String::class.java) {
                                val annotationActionBean = AnnotationActionBean()
                                val requestMappingAnnotation = method.getAnnotation(RequestMapping::class.java!!)
                                annotationActionBean.type = clazz.getName()
                                val httpRequestMethods = requestMappingAnnotation.httpRequestMethods()
                                if (httpRequestMethods != null && httpRequestMethods!!.size > 0) {
                                    val stringBuilder = StringBuilder()
                                    for (i in httpRequestMethods!!.indices) {
                                        stringBuilder.append(httpRequestMethods!![i])
                                        if (i < httpRequestMethods!!.size - 1) {
                                            stringBuilder.append(Constants.Symbol.COMMA)
                                        }
                                    }
                                    annotationActionBean.httpRequestMethods = stringBuilder.toString()
                                }
                                val httpRequestMethodsCode = annotationActionBean.httpRequestMethodsCode
                                val id = classId + Constants.Symbol.DOT + method.getName() + Constants.Symbol.COMMA + httpRequestMethodsCode
                                annotationActionBean.id = id
                                val requestPath = requestMappingAnnotation.value()
                                annotationActionBean.path = requestPath
                                annotationActionBean.method = method
                                annotationActionBean.actionInstance = actionInstance
                                ActionContext.actionBeanMap[id] = annotationActionBean
                                var actionBeanList: MutableList<ActionBean>? = null
                                if (ActionContext.pathActionBeanMap.containsKey(requestPath)) {
                                    actionBeanList = ActionContext.pathActionBeanMap[requestPath]
                                } else {
                                    actionBeanList = ArrayList()
                                    ActionContext.pathActionBeanMap[requestPath] = actionBeanList
                                }
                                actionBeanList!!.add(annotationActionBean)
                                //interceptor
                                val interceptors = requestMappingAnnotation.interceptors()
                                if (interceptors != null && interceptors!!.size > 0) {
                                    for (interceptor in interceptors!!) {
                                        val interceptorId = interceptor.id()
                                        val interceptorMode = interceptor.mode()
                                        if (StringUtil.isNotBlank(interceptorId)) {
                                            val actionInterceptorBean = ActionInterceptorBean()
                                            actionInterceptorBean.id = interceptorId
                                            when (interceptorMode) {
                                                BEFORE -> actionInterceptorBean.mode = ActionInterceptorBean.INTERCEPTOR_MODE_BEFORE
                                                AFTER -> actionInterceptorBean.mode = ActionInterceptorBean.INTERCEPTOR_MODE_AFTER
                                            }
                                            annotationActionBean.addActionBeanInterceptor(actionInterceptorBean)
                                        }
                                    }
                                }
                                //static
                                val statics = requestMappingAnnotation.statics()
                                if (statics != null && statics!!.size > 0) {
                                    for (staticAnnotation in statics!!) {
                                        val actionForwardBean = ActionForwardBean()
                                        actionForwardBean.staticParameters = staticAnnotation.parameters()
                                        actionForwardBean.staticFilePath = staticAnnotation.filePath()
                                        annotationActionBean.addActionForwardBean(actionForwardBean)
                                    }
                                }
                            } else {
                                throw InitializeException("@" + RequestMapping::class.java!!.getSimpleName() + " method:" + method.getName() + " which the return type must be String.class,current is:" + returnType)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }

    }
}
