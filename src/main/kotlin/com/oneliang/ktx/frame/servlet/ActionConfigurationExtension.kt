package com.oneliang.ktx.frame.servlet

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ConfigurationFactory
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.ioc.IocBean
import com.oneliang.ktx.frame.ioc.IocContext
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
 * inject
 */
@Throws(Exception::class)
fun ConfigurationContext.inject() {
    this.iocInject()
    this.interceptorInject()
//        processorInject()
}

/**
 * ioc inject
 */
@Throws(Exception::class)
private fun ConfigurationContext.iocInject() {
    val iocContext = this.findContext(IocContext::class)
    iocContext?.inject()
}

/**
 * interceptor inject
 */
fun ConfigurationContext.interceptorInject() {
    val actionContext = this.findContext(ActionContext::class)
    actionContext?.interceptorInject()
}

/**
 * ioc auto inject object by id
 *
 * @param id
 * @param instance
 * @throws Exception
 */
@Throws(Exception::class)
fun ConfigurationContext.iocAutoInjectObjectById(id: String, instance: Any) {
    val iocContext = this.findContext(IocContext::class)
    if (iocContext != null) {
        val iocBean = IocBean()
        iocBean.id = id
        iocBean.injectType = IocBean.INJECT_TYPE_AUTO_BY_ID
        iocBean.proxy = false
        iocBean.proxyInstance = instance
        iocBean.beanInstance = instance
        iocBean.type = instance.javaClass.name
        iocContext.putToIocBeanMap(id, iocBean)
        iocContext.autoInjectObjectById(instance)
    }
}

/**
 * put object to ioc bean map
 *
 * @param id
 * @param instance
 */
fun ConfigurationContext.putObjectToIocBeanMap(id: String, instance: Any) {
    val iocContext = this.findContext(IocContext::class)
    if (iocContext != null) {
        val iocBean = IocBean()
        iocBean.id = id
        iocBean.injectType = IocBean.INJECT_TYPE_AUTO_BY_ID
        iocBean.proxy = false
        iocBean.proxyInstance = instance
        iocBean.beanInstance = instance
        iocBean.type = instance.javaClass.name
        iocContext.putToIocBeanMap(id, iocBean)
    }
}

/**
 * after inject
 */
@Throws(Exception::class)
fun ConfigurationContext.afterInject() {
    val iocContext = this.findContext(IocContext::class)
    iocContext?.afterInject()
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

/**
 * output action map
 */
fun ConfigurationContext.outputActionMap(outputFilename: String) {
    val sortedActionBeanMap = ActionContext.actionBeanMap.toSortedMap()
    val bufferedWriter = BufferedWriter(FileWriter(File(this.projectRealPath, outputFilename)))
    bufferedWriter.use {
        sortedActionBeanMap.forEach { (_, actionBean) ->
            it.newLine()
            it.write("uri:${actionBean.path}")
            it.newLine()
            it.write("\tmethods:${actionBean.httpRequestMethods}")
            it.newLine()
            if (actionBean is AnnotationActionBean) {
                val annotationActionBeanMethod = actionBean.method!!
                val classes = annotationActionBeanMethod.parameterTypes
                val parameterAnnotations = annotationActionBeanMethod.parameterAnnotations
                parameterAnnotations.forEachIndexed { index, annotationArray ->
                    if (annotationArray.isNotEmpty() && annotationArray[0] is Action.RequestMapping.RequestParameter) {
                        val parameterAnnotation = annotationArray[0] as Action.RequestMapping.RequestParameter
                        it.write("\tparameter$index(${parameterAnnotation.value}):${classes[index].name}")
                        it.newLine()
                    }
                }
            }
            it.flush()
        }
    }
}