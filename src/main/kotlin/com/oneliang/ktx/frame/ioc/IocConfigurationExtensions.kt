package com.oneliang.ktx.frame.ioc

import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.servlet.interceptorInject

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