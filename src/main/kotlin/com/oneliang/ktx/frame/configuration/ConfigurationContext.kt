package com.oneliang.ktx.frame.configuration

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.frame.context.Context
import com.oneliang.ktx.util.common.JavaXmlUtil
import com.oneliang.ktx.util.common.ObjectUtil
import com.oneliang.ktx.util.logging.LoggerManager
import kotlin.collections.Map.Entry
import kotlin.reflect.KClass

class ConfigurationContext : AbstractContext() {
    companion object {
        private val logger = LoggerManager.getLogger(ConfigurationContext::class)
        internal val configurationBeanMap = mutableMapOf<String, ConfigurationBean>()
    }

    private val selfConfigurationBeanMap = mutableMapOf<String, ConfigurationBean>()
    /**
     * @return the initialized
     */
    var initialized = false
        protected set

    /**
     * get configuration bean entry set
     * @return the configurationBeanMap
     */
    val configurationBeanEntrySet: Set<Entry<String, ConfigurationBean>>
        get() = configurationBeanMap.entries

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        try {
            var path = parameters
            val tempClassesRealPath = if (classesRealPath.isBlank()) {
                this.classLoader.getResource(Constants.String.BLANK).path
            } else {
                classesRealPath
            }
            path = tempClassesRealPath + path
            val document = JavaXmlUtil.parse(path)
            val root = document.documentElement
            val configurationList = root.getElementsByTagName(ConfigurationBean.TAG_CONFIGURATION)
            if (configurationList != null) {
                val length = configurationList.length
                for (index in 0 until length) {
                    val configurationBean = ConfigurationBean()
                    val configurationNode = configurationList.item(index)
                    val configurationAttributesMap = configurationNode.attributes
                    JavaXmlUtil.initializeFromAttributeMap(configurationBean, configurationAttributesMap)
                    val context = this.classLoader.loadClass(configurationBean.contextClass).newInstance() as Context
                    logger.info("Context:" + context.javaClass.name + ",id:" + configurationBean.id + " is initializing.")
                    if (context is AbstractContext) {
                        context.projectRealPath = this.projectRealPath
                        context.classesRealPath = this.classesRealPath
                    }
                    context.initialize(configurationBean.parameters)
                    configurationBean.contextInstance = context
                    configurationBeanMap[configurationBean.id] = configurationBean
                    this.selfConfigurationBeanMap[configurationBean.id] = configurationBean
                }
            }
            this.initialized = true
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }
    }

    /**
     * destroy,only destroy self,recursion
     */
    override fun destroy() {
        val iterator = this.selfConfigurationBeanMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val configurationBean = entry.value
            configurationBean.contextInstance?.destroy()
        }
        this.selfConfigurationBeanMap.clear()
    }

    /**
     * destroy all,include destroy all configuration context,all configuration bean and all object
     */
    fun destroyAll() {
        this.destroy()
        configurationBeanMap.clear()
        objectMap.clear()
    }

    /**
     * find context
     * @param id
     * @return Context
     */
    fun findContext(id: String): Context? {
        return configurationBeanMap[id]?.contextInstance
    }


    /**
     * find context
     * @param <T>
     * @param clazz
     * @return T
    </T> */
    @Suppress("UNCHECKED_CAST")
    fun <T : Context> findContext(clazz: KClass<T>): T? {
        var instance: T? = null
        run loop@{
            configurationBeanMap.forEach { (_, value) ->
                val context = value.contextInstance
                if (ObjectUtil.isEntity(context as Any, clazz.java)) {
                    instance = context as T
                    return@loop
                }
            }
        }
        return instance
    }
}