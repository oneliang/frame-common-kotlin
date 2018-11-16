package com.oneliang.ktx.frame

import com.oneliang.ktx.Constants

import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.ioc.IocBean
import com.oneliang.ktx.frame.ioc.IocContext
import com.oneliang.ktx.frame.servlet.action.ActionBean
import com.oneliang.ktx.frame.servlet.action.ActionContext
import com.oneliang.ktx.frame.servlet.action.Interceptor
import com.oneliang.ktx.frame.servlet.action.InterceptorContext

/**
 * ConfigurationFactory
 *
 * @author Dandelion
 * @since 2009-03-12
 */
object ConfigurationFactory {

    /**
     * get singleton configuration context
     *
     * @return ConfigurationContext
     */
    val singletonConfigurationContext = ConfigurationContext()

    /**
     * get before global interceptor list
     *
     * @return List<Interceptor>
    </Interceptor> */
    val beforeGlobalInterceptorList: List<Interceptor>
        get() {
            var beforeGlobalInterceptorList: List<Interceptor> = emptyList()
            val interceptorContext = singletonConfigurationContext.findContext(InterceptorContext::class)
            if (interceptorContext != null) {
                beforeGlobalInterceptorList = interceptorContext.getBeforeGlobalInterceptorList()
            }
            return beforeGlobalInterceptorList
        }

    /**
     * get after global interceptor list
     *
     * @return List<Interceptor>
    </Interceptor> */
    val afterGlobalInterceptorList: List<Interceptor>
        get() {
            var afterGlobalInterceptorList: List<Interceptor> = emptyList()
            val interceptorContext = singletonConfigurationContext.findContext(InterceptorContext::class)
            if (interceptorContext != null) {
                afterGlobalInterceptorList = interceptorContext!!.getAfterGlobalInterceptorList()
            }
            return afterGlobalInterceptorList
        }

    /**
     * get global exception forward path
     *
     * @return String
     */
    val globalExceptionForwardPath: String?
        get() {
            val actionContext = singletonConfigurationContext.findContext(ActionContext::class)
            return actionContext?.globalExceptionForwardPath ?: Constants.String.BLANK
        }

    /**
     * get mapping bean entry set
     *
     * @return Set<Entry></Entry><String></String>,MappingBean>>
     * @throws Exception
     */
//    val mappingBeanEntrySet: Set<Entry<String, MappingBean>>?
//        get() {
//            var mappingBeanEntrySet: Set<Entry<String, MappingBean>>? = null
//            val mappingContext = singletonConfigurationContext.findContext(MappingContext::class.java)
//            if (mappingContext != null) {
//                mappingBeanEntrySet = mappingContext!!.getMappingBeanEntrySet()
//            }
//            return mappingBeanEntrySet
//        }

    /**
     * injection,include ioc injection and interceptor injection
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    fun inject() {
        iocInject()
        interceptorInject()
        processorInject()
    }

    /**
     * ioc injection
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    fun iocInject() {
        val iocContext = singletonConfigurationContext.findContext(IocContext::class)
        if (iocContext != null) {
            iocContext.inject()
        }
    }

    /**
     * after inject
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    fun afterInject() {
        val iocContext = singletonConfigurationContext.findContext(IocContext::class.java)
        if (iocContext != null) {
            iocContext!!.afterInject()
        }
    }

    /**
     * interceptor inject
     */
    fun interceptorInject() {
        val actionContext = singletonConfigurationContext.findContext(ActionContext::class.java)
        if (actionContext != null) {
            actionContext!!.interceptorInject()
        }
    }

    /**
     * processor injection
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    fun processorInject() {
        val iterator = singletonConfigurationContext.getConfigurationBeanEntrySet().iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val configurationBean = entry.value
            val context = configurationBean.getContextInstance()
            if (context is TaskContext) {
                val taskContext = context as TaskContext
                taskContext.processorInject()
            }
        }
    }

    /**
     * ioc auto inject object by id
     *
     * @param id
     * @param object
     * @throws Exception
     */
    @Throws(Exception::class)
    fun iocAutoInjectObjectById(id: String, instance: Any?) {
        if (instance != null) {
            val iocContext = singletonConfigurationContext.findContext(IocContext::class.java)
            if (iocContext != null) {
                val iocBean = IocBean()
                iocBean.setId(id)
                iocBean.setInjectType(IocBean.INJECT_TYPE_AUTO_BY_ID)
                iocBean.setProxy(false)
                iocBean.setProxyInstance(instance)
                iocBean.setBeanInstance(instance)
                iocBean.setType(instance.javaClass.name)
                iocContext!!.putToIocBeanMap(id, iocBean)
                iocContext!!.autoInjectObjectById(instance)
            }
        }
    }

    /**
     * put object to ioc bean map
     *
     * @param id
     * @param object
     */
    fun putObjectToIocBeanMap(id: String, instance: Any) {
        val iocContext = singletonConfigurationContext.findContext(IocContext::class)
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
     * initial connection pools
     *
     * @throws Exception
     */
    @Deprecated("")
    @Throws(Exception::class)
    fun initialConnectionPools() {
        val dataBaseContext = singletonConfigurationContext.findContext(DatabaseContext::class.java)
        if (dataBaseContext != null) {
            dataBaseContext!!.initialConnectionPools()
        }
    }

    /**
     * find global forward path with name
     *
     * @param name
     * @return String
     */
    fun findGlobalForwardPath(name: String?): String? {
        var path: String? = null
        if (name != null) {
            val actionContext = singletonConfigurationContext.findContext(ActionContext::class.java)
            if (actionContext != null) {
                path = actionContext!!.findGlobalForwardPath(name)
            }
        }
        return path
    }

    /**
     * find bean
     *
     * @param id
     * @return T
     */
    fun <T : Any> findBean(id: String): T {
        return singletonConfigurationContext.findBean(id)
    }

    /**
     * find ActionBean list
     *
     * @param uri
     * @return List<ActionBean>
    </ActionBean> */
    fun findActionBeanList(uri: String): List<ActionBean>? {
        var actionBeanList: List<ActionBean>? = null
        val actionContext = singletonConfigurationContext.findContext(ActionContext::class)
        if (actionContext != null) {
            actionBeanList = actionContext.findActionBeanList(uri)
        }
        return actionBeanList
    }

    /**
     * find mappingBean
     *
     * @param <T>
     * @param clazz
     * @return MappingBean
    </T> */
    fun <T : Any> findMappingBean(clazz: Class<T>?): MappingBean? {
        var mappingBean: MappingBean? = null
        if (clazz != null) {
            val mappingContext = singletonConfigurationContext.findContext(MappingContext::class.java)
            if (mappingContext != null) {
                mappingBean = mappingContext!!.findMappingBean(clazz)
            }
        }
        return mappingBean
    }

    /**
     * find mappingBean
     *
     * @param name
     * @return MappingBean
     */
    fun findMappingBean(name: String?): MappingBean? {
        var mappingBean: MappingBean? = null
        if (name != null) {
            val mappingContext = singletonConfigurationContext.findContext(MappingContext::class.java)
            if (mappingContext != null) {
                mappingBean = mappingContext!!.findMappingBean(name)
            }
        }
        return mappingBean
    }

}
