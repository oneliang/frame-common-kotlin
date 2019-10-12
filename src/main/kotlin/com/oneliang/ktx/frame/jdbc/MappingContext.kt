package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.util.common.JavaXmlUtil
import kotlin.reflect.KClass

open class MappingContext : AbstractContext() {
    companion object {
        internal val classNameMappingBeanMap = mutableMapOf<String, MappingBean>()
        internal val simpleNameMappingBeanMap = mutableMapOf<String, MappingBean>()

        init {
            val totalMappingBean = Total.toMappingBean()
            val totalClassName = totalMappingBean.type
            classNameMappingBeanMap[totalClassName] = totalMappingBean
            simpleNameMappingBeanMap[Total::class.java.simpleName] = totalMappingBean
        }
    }

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        try {
            val path = this.classesRealPath + parameters
            val document = JavaXmlUtil.parse(path)
            val root = document.documentElement
            val beanElementList = root.getElementsByTagName(MappingBean.TAG_BEAN)
            if (beanElementList != null) {
                val length = beanElementList.length
                for (index in 0 until length) {
                    val beanElement = beanElementList.item(index)
                    val mappingBean = MappingBean()
                    val attributeMap = beanElement.attributes
                    JavaXmlUtil.initializeFromAttributeMap(mappingBean, attributeMap)
                    //bean column
                    val childNodeList = beanElement.childNodes ?: continue
                    val childNodeLength = childNodeList.length
                    for (childNodeIndex in 0 until childNodeLength) {
                        val childNode = childNodeList.item(childNodeIndex)
                        val nodeName = childNode.nodeName
                        if (nodeName == MappingColumnBean.TAG_COLUMN) {
                            val mappingColumnBean = MappingColumnBean()
                            val childNodeAttributeMap = childNode.attributes
                            JavaXmlUtil.initializeFromAttributeMap(mappingColumnBean, childNodeAttributeMap)
                            mappingBean.addMappingColumnBean(mappingColumnBean)
                        }
                    }
                    val className = mappingBean.type
                    classNameMappingBeanMap[className] = mappingBean
                    simpleNameMappingBeanMap[this.classLoader.loadClass(className).simpleName] = mappingBean
                }
            }
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }
    }

    /**
     * destroy
     */
    override fun destroy() {
        classNameMappingBeanMap.clear()
        simpleNameMappingBeanMap.clear()
    }

    /**
     * findMappingBean
     * @param <T>
     * @param clazz
     * @return MappingBean
    </T> */
    fun <T : Any> findMappingBean(clazz: KClass<T>): MappingBean? {
        val className = clazz.java.name
        return classNameMappingBeanMap[className]
    }

    /**
     * @param name full name or simple name
     * @return MappingBean
     */
    fun findMappingBean(name: String): MappingBean? {
        var bean: MappingBean?
        bean = classNameMappingBeanMap[name]
        if (bean == null) {
            bean = simpleNameMappingBeanMap[name]
        }
        return bean
    }
}
