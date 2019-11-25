package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AnnotationContextUtil

class AnnotationActionContext : ActionContext() {

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        try {
            val kClassList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(parameters, classLoader, classesRealPath, jarClassLoader, Action::class)
            for (kClass in kClassList) {
                val classId = kClass.java.name
                val actionInstance: Any
                if (!objectMap.containsKey(classId)) {
                    actionInstance = kClass.java.newInstance()
                    objectMap[classId] = actionInstance
                } else {
                    actionInstance = objectMap[classId]!!
                }
                val methods = kClass.java.methods
                for (method in methods) {
                    if (!method.isAnnotationPresent(Action.RequestMapping::class.java)) {
                        continue
                    }
                    val returnType = method.returnType
                    if (returnType != null && returnType == String::class.java) {
                        val annotationActionBean = AnnotationActionBean()
                        val requestMappingAnnotation = method.getAnnotation(Action.RequestMapping::class.java)
                        annotationActionBean.type = kClass.java.name
                        val annotationHttpRequestMethods = requestMappingAnnotation.httpRequestMethods
                        val httpRequestMethods = if (annotationHttpRequestMethods.isNotEmpty()) {
                            annotationHttpRequestMethods
                        } else {
                            arrayOf(Constants.Http.RequestMethod.GET, Constants.Http.RequestMethod.POST)
                        }
                        if (httpRequestMethods.isNotEmpty()) {
                            val stringBuilder = StringBuilder()
                            for (i in httpRequestMethods.indices) {
                                stringBuilder.append(httpRequestMethods[i].value)
                                if (i < httpRequestMethods.size - 1) {
                                    stringBuilder.append(Constants.Symbol.COMMA)
                                }
                            }
                            annotationActionBean.httpRequestMethods = stringBuilder.toString()
                        }
                        val httpRequestMethodsCode = annotationActionBean.httpRequestMethodsCode
                        val id = classId + Constants.Symbol.DOT + method.name + Constants.Symbol.COMMA + httpRequestMethodsCode
                        annotationActionBean.id = id
                        val requestPath = requestMappingAnnotation.value
                        annotationActionBean.path = requestPath
                        annotationActionBean.method = method
                        annotationActionBean.actionInstance = actionInstance
                        ActionContext.actionBeanMap[id] = annotationActionBean
                        val actionBeanList: MutableList<ActionBean>
                        if (ActionContext.pathActionBeanMap.containsKey(requestPath)) {
                            actionBeanList = ActionContext.pathActionBeanMap[requestPath]!!
                        } else {
                            actionBeanList = mutableListOf()
                            ActionContext.pathActionBeanMap[requestPath] = actionBeanList
                        }
                        actionBeanList.add(annotationActionBean)
                        //interceptor
                        val interceptors = requestMappingAnnotation.interceptors
                        for (interceptor in interceptors) {
                            val interceptorId = interceptor.id
                            val interceptorMode = interceptor.mode
                            if (interceptorId.isBlank()) {
                                continue
                            }
                            val actionInterceptorBean = ActionInterceptorBean()
                            actionInterceptorBean.id = interceptorId
                            when (interceptorMode) {
                                Action.RequestMapping.Interceptor.Mode.BEFORE -> actionInterceptorBean.mode = ActionInterceptorBean.Mode.BEFORE
                                Action.RequestMapping.Interceptor.Mode.AFTER -> actionInterceptorBean.mode = ActionInterceptorBean.Mode.AFTER
                            }
                            annotationActionBean.addActionBeanInterceptor(actionInterceptorBean)
                        }
                        //static
                        val statics = requestMappingAnnotation.statics
                        for (staticAnnotation in statics) {
                            val actionForwardBean = ActionForwardBean()
                            actionForwardBean.staticParameters = staticAnnotation.parameters
                            actionForwardBean.staticFilePath = staticAnnotation.filePath
                            annotationActionBean.addActionForwardBean(actionForwardBean)
                        }
                    } else {
                        throw InitializeException("@" + Action.RequestMapping::class.java.simpleName + "class:" + kClass.java.name + ", method:" + method.name + " which the return type must be String.class,current is:" + returnType)
                    }
                }
            }
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }
    }
}
