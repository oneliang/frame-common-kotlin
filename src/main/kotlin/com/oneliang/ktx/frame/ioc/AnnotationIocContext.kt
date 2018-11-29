package com.oneliang.ktx.frame.ioc

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AnnotationContextUtil
import com.oneliang.ktx.util.logging.LoggerManager

class AnnotationIocContext : IocContext() {

    companion object {
        private val logger = LoggerManager.getLogger(AnnotationIocContext::class)
    }

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        try {
            val classList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(parameters, classLoader, classesRealPath, jarClassLoader, projectRealPath, Ioc::class)
            for (clazz in classList) {
                logger.debug("found class:$clazz")
                val iocAnnotation = clazz.java.getAnnotation(Ioc::class.java)
                val iocBean = IocBean()
                var id = iocAnnotation.id
                if (id.isBlank()) {
                    val classes = clazz.java.interfaces
                    if (classes != null && classes.isNotEmpty()) {
                        id = classes[0].simpleName
                    } else {
                        id = clazz.java.simpleName
                    }
                    id = id.substring(0, 1).toLowerCase() + id.substring(1)
                }
                iocBean.id = id
                iocBean.type = clazz.java.name
                iocBean.injectType = iocAnnotation.injectType
                iocBean.proxy = iocAnnotation.proxy
                iocBean.beanClass = clazz.java
                //after inject
                val methods = clazz.java.methods
                for (method in methods) {
                    if (method.isAnnotationPresent(Ioc.AfterInject::class.java)) {
                        val iocAfterInjectBean = IocAfterInjectBean()
                        iocAfterInjectBean.method = method.name
                        iocBean.addIocAfterInjectBean(iocAfterInjectBean)
                    }
                }
                IocContext.iocBeanMap[iocBean.id] = iocBean
            }
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }
    }
}
