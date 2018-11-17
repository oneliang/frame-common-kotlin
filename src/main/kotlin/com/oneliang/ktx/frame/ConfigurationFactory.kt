package com.oneliang.ktx.frame

import com.oneliang.ktx.frame.configuration.ConfigurationContext

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
     * processor injection
     *
     * @throws Exception
     */
//    @Throws(Exception::class)
//    fun processorInject() {
//        val iterator = singletonConfigurationContext.getConfigurationBeanEntrySet().iterator()
//        while (iterator.hasNext()) {
//            val entry = iterator.next()
//            val configurationBean = entry.value
//            val context = configurationBean.getContextInstance()
//            if (context is TaskContext) {
//                val taskContext = context as TaskContext
//                taskContext.processorInject()
//            }
//        }
//    }

    /**
     * initial connection pools
     *
     * @throws Exception
     */
//    @Deprecated("")
//    @Throws(Exception::class)
//    fun initialConnectionPools() {
//        val dataBaseContext = singletonConfigurationContext.findContext(DatabaseContext::class.java)
//        if (dataBaseContext != null) {
//            dataBaseContext!!.initialConnectionPools()
//        }
//    }




    /**
     * find mappingBean
     *
     * @param <T>
     * @param clazz
     * @return MappingBean
    </T> */
//    fun <T : Any> findMappingBean(clazz: Class<T>?): MappingBean? {
//        var mappingBean: MappingBean? = null
//        if (clazz != null) {
//            val mappingContext = singletonConfigurationContext.findContext(MappingContext::class.java)
//            if (mappingContext != null) {
//                mappingBean = mappingContext!!.findMappingBean(clazz)
//            }
//        }
//        return mappingBean
//    }

    /**
     * find mappingBean
     *
     * @param name
     * @return MappingBean
     */
//    fun findMappingBean(name: String?): MappingBean? {
//        var mappingBean: MappingBean? = null
//        if (name != null) {
//            val mappingContext = singletonConfigurationContext.findContext(MappingContext::class.java)
//            if (mappingContext != null) {
//                mappingBean = mappingContext!!.findMappingBean(name)
//            }
//        }
//        return mappingBean
//    }

}
