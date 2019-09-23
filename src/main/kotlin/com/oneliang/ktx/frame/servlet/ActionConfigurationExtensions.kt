package com.oneliang.ktx.frame.servlet

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ConfigurationFactory
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.servlet.action.*
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

/**
 * before global interceptor list
 */
val ConfigurationContext.beforeGlobalInterceptorList: List<InterceptorInterface>
    get() {
        var beforeGlobalInterceptorList: List<InterceptorInterface> = emptyList()
        val interceptorContext = this.findContext(InterceptorContext::class)
        if (interceptorContext != null) {
            beforeGlobalInterceptorList = interceptorContext.getBeforeGlobalInterceptorList()
        }
        return beforeGlobalInterceptorList
    }

/**
 * after global interceptor list
 */
val ConfigurationContext.afterGlobalInterceptorList: List<InterceptorInterface>
    get() {
        var afterGlobalInterceptorList: List<InterceptorInterface> = emptyList()
        val interceptorContext = this.findContext(InterceptorContext::class)
        if (interceptorContext != null) {
            afterGlobalInterceptorList = interceptorContext.getAfterGlobalInterceptorList()
        }
        return afterGlobalInterceptorList
    }

/**
 * global exception forward path
 */
val ConfigurationContext.globalExceptionForwardPath: String?
    get() {
        val actionContext = this.findContext(ActionContext::class)
        return actionContext?.globalExceptionForwardPath ?: Constants.String.BLANK
    }

/**
 * interceptor inject
 */
fun ConfigurationContext.interceptorInject() {
    val actionContext = this.findContext(ActionContext::class)
    actionContext?.interceptorInject()
}

/**
 * find global forward path with name
 *
 * @param name
 * @return String
 */
fun ConfigurationContext.findGlobalForwardPath(name: String): String {
    val actionContext = ConfigurationFactory.singletonConfigurationContext.findContext(ActionContext::class)
    return actionContext?.findGlobalForwardPath(name) ?: Constants.String.BLANK
}


/**
 * find ActionBean list
 *
 * @param uri
 * @return List<ActionBean>
</ActionBean> */
fun ConfigurationContext.findActionBeanList(uri: String): List<ActionBean>? {
    val actionContext = ConfigurationFactory.singletonConfigurationContext.findContext(ActionContext::class)
    return actionContext?.findActionBeanList(uri)
}