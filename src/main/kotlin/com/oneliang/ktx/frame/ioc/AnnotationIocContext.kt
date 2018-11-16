package com.oneliang.ktx.frame.ioc

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.AbstractContext.Companion.jarClassLoader
import com.oneliang.ktx.frame.AnnotationContextUtil
import com.oneliang.ktx.util.common.StringUtil

class AnnotationIocContext : IocContext() {

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        try {
            val classList = AnnotationContextUtil.parseAnnotationContextParameter(parameters, classLoader, classesRealPath, jarClassLoader, projectRealPath, Ioc::class)
            if (classList != null) {
                for (clazz in classList!!) {
                    val iocAnnotation = clazz.getAnnotation(Ioc::class.java)
                    val iocBean = IocBean()
                    var id = iocAnnotation.id()
                    if (StringUtil.isBlank(id)) {
                        val classes = clazz.getInterfaces()
                        if (classes != null && classes!!.size > 0) {
                            id = classes!![0].getSimpleName()
                        } else {
                            id = clazz.getSimpleName()
                        }
                        id = id.substring(0, 1).toLowerCase() + id.substring(1)
                    }
                    iocBean.id = id
                    iocBean.type = clazz.getName()
                    iocBean.injectType = iocAnnotation.injectType()
                    iocBean.proxy = iocAnnotation.proxy()
                    iocBean.beanClass = clazz
                    //after inject
                    val methods = clazz.getMethods()
                    for (method in methods) {
                        if (method.isAnnotationPresent(Ioc.AfterInject::class.java)) {
                            val iocAfterInjectBean = IocAfterInjectBean()
                            iocAfterInjectBean.method = method.getName()
                            iocBean.addIocAfterInjectBean(iocAfterInjectBean)
                        }
                    }
                    IocContext.iocBeanMap[iocBean.id] = iocBean
                }
            }
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }

    }
}
