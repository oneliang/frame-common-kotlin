package com.oneliang.ktx.frame.configuration

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.AbstractContext
import com.oneliang.ktx.frame.Context
import com.oneliang.ktx.util.common.JavaXmlUtil
import com.oneliang.ktx.util.common.ObjectUtil
import com.oneliang.ktx.util.logging.LoggerManager
import kotlin.collections.Map.Entry

class ConfigurationContext : AbstractContext() {
    companion object {
        private val logger = LoggerManager.getLogger(ConfigurationContext::class.java)
        internal val configurationBeanMap = mutableMapOf<String, ConfigurationBean>()
    }

    private val selfConfigurationBeanMap = mutableMapOf<String, ConfigurationBean>()
    /**
     * @return the initialized
     */
    var isInitialized = false
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
            var tempClassesRealPath = classesRealPath
            if (tempClassesRealPath == null) {
                tempClassesRealPath = this.classLoader.getResource(Constants.String.BLANK).getPath()
            }
            path = tempClassesRealPath!! + path
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
                        val abstractContext = context as AbstractContext
                        abstractContext.projectRealPath = this.projectRealPath
                        abstractContext.projectRealPath = this.classesRealPath
                    }
                    context.initialize(configurationBean.parameters)
                    configurationBean.contextInstance = context
                    configurationBeanMap[configurationBean.id] = configurationBean
                    this.selfConfigurationBeanMap[configurationBean.id] = configurationBean
                }
            }
            this.isInitialized = true
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
    fun <T : Context> findContext(clazz: Class<T>): T? {
        var instance: T? = null
        val iterator = configurationBeanMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val configurationBean = entry.value
            val context = configurationBean.contextInstance
            if (ObjectUtil.isEntity(context as Any, clazz)) {
                instance = context as T
                break
            }
        }
        return instance
    }
}
