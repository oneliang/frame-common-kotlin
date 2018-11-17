package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AnnotationContextUtil
import com.oneliang.ktx.util.common.ObjectUtil

class AnnotationInterceptorContext : InterceptorContext() {

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        try {
            val classList = AnnotationContextUtil.parseAnnotationContextParameter(parameters, classLoader, classesRealPath, jarClassLoader, projectRealPath, ActionInterceptor::class)
            for (clazz in classList) {
                if (ObjectUtil.isInheritanceOrInterfaceImplement(clazz.java, Interceptor::class.java)) {
                    val interceptorAnnotation = clazz.java.getAnnotation(ActionInterceptor::class.java)
                    val interceptorMode = interceptorAnnotation.mode
                    var id = interceptorAnnotation.id
                    if (id.isBlank()) {
                        id = clazz.java.simpleName
                        id = id.substring(0, 1).toLowerCase() + id.substring(1)
                    }
                    val interceptorInstance = clazz.java.newInstance() as Interceptor
                    when (interceptorMode) {
                        ActionInterceptor.Mode.GLOBAL_ACTION_BEFORE -> {
                            val globalBeforeInterceptor = GlobalInterceptorBean()
                            globalBeforeInterceptor.id = id
                            globalBeforeInterceptor.mode = GlobalInterceptorBean.INTERCEPTOR_MODE_BEFORE
                            globalBeforeInterceptor.interceptorInstance = interceptorInstance
                            globalBeforeInterceptor.type = clazz.java.name
                            InterceptorContext.globalInterceptorBeanMap[globalBeforeInterceptor.id] = globalBeforeInterceptor
                            InterceptorContext.beforeGlobalInterceptorList.add(interceptorInstance)
                        }
                        ActionInterceptor.Mode.GLOBAL_ACTION_AFTER -> {
                            val globalAfterInterceptor = GlobalInterceptorBean()
                            globalAfterInterceptor.id = id
                            globalAfterInterceptor.mode = GlobalInterceptorBean.INTERCEPTOR_MODE_AFTER
                            globalAfterInterceptor.interceptorInstance = interceptorInstance
                            globalAfterInterceptor.type = clazz.java.name
                            InterceptorContext.globalInterceptorBeanMap[globalAfterInterceptor.id] = globalAfterInterceptor
                            InterceptorContext.afterGlobalInterceptorList.add(interceptorInstance)
                        }
                        ActionInterceptor.Mode.SINGLE_ACTION -> {
                            val interceptor = InterceptorBean()
                            interceptor.id = id
                            interceptor.interceptorInstance = interceptorInstance
                            interceptor.type = clazz.java.name
                            InterceptorContext.interceptorBeanMap[interceptor.id] = interceptor
                        }
                    }
                    objectMap.put(id, interceptorInstance)
                }
            }
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }

    }
}