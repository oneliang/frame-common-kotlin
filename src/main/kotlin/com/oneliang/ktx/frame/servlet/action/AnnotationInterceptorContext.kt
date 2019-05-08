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
            val classList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(parameters, classLoader, classesRealPath, jarClassLoader, Interceptor::class)
            for (clazz in classList) {
                if (ObjectUtil.isInheritanceOrInterfaceImplement(clazz.java, InterceptorInterface::class.java)) {
                    val interceptorAnnotation = clazz.java.getAnnotation(Interceptor::class.java)
                    val interceptorMode = interceptorAnnotation.mode
                    var id = interceptorAnnotation.id
                    if (id.isBlank()) {
                        id = clazz.java.simpleName
                        id = id.substring(0, 1).toLowerCase() + id.substring(1)
                    }
                    val interceptorInstance = clazz.java.newInstance() as InterceptorInterface
                    when (interceptorMode) {
                        Interceptor.Mode.GLOBAL_ACTION_BEFORE -> {
                            val globalBeforeInterceptor = GlobalInterceptorBean()
                            globalBeforeInterceptor.id = id
                            globalBeforeInterceptor.mode = GlobalInterceptorBean.INTERCEPTOR_MODE_BEFORE
                            globalBeforeInterceptor.interceptorInstance = interceptorInstance
                            globalBeforeInterceptor.type = clazz.java.name
                            InterceptorContext.globalInterceptorBeanMap[globalBeforeInterceptor.id] = globalBeforeInterceptor
                            InterceptorContext.beforeGlobalInterceptorList.add(interceptorInstance)
                        }
                        Interceptor.Mode.GLOBAL_ACTION_AFTER -> {
                            val globalAfterInterceptor = GlobalInterceptorBean()
                            globalAfterInterceptor.id = id
                            globalAfterInterceptor.mode = GlobalInterceptorBean.INTERCEPTOR_MODE_AFTER
                            globalAfterInterceptor.interceptorInstance = interceptorInstance
                            globalAfterInterceptor.type = clazz.java.name
                            InterceptorContext.globalInterceptorBeanMap[globalAfterInterceptor.id] = globalAfterInterceptor
                            InterceptorContext.afterGlobalInterceptorList.add(interceptorInstance)
                        }
                        Interceptor.Mode.SINGLE_ACTION -> {
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
