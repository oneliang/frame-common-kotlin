package com.oneliang.ktx.frame.jxl

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.util.common.JavaXmlUtil
import com.oneliang.ktx.util.jxl.JxlMappingBean
import com.oneliang.ktx.util.jxl.JxlMappingColumnBean
import java.util.concurrent.ConcurrentHashMap

class JxlMappingContext : AbstractContext() {
    companion object {
        internal val typeImportJxlMappingBeanMap = ConcurrentHashMap<String, JxlMappingBean>()
        internal val nameImportJxlMappingBeanMap = ConcurrentHashMap<String, JxlMappingBean>()
        internal val typeExportJxlMappingBeanMap = ConcurrentHashMap<String, JxlMappingBean>()
        internal val nameExportJxlMappingBeanMap = ConcurrentHashMap<String, JxlMappingBean>()
    }

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        try {
            var path = parameters
            path = classesRealPath + path
            val document = JavaXmlUtil.parse(path)
            val root = document.documentElement
            val beanElementList = root.getElementsByTagName(JxlMappingBean.TAG_BEAN)
            if (beanElementList != null) {
                val length = beanElementList.length
                for (index in 0 until length) {
                    val beanElement = beanElementList.item(index)
                    val jxlMappingBean = JxlMappingBean()
                    val attributeMap = beanElement.getAttributes()
                    JavaXmlUtil.initializeFromAttributeMap(jxlMappingBean, attributeMap)
                    //bean column
                    val childNodeList = beanElement.getChildNodes()
                    if (childNodeList != null) {
                        val childNodeLength = childNodeList.length
                        for (childNodeIndex in 0 until childNodeLength) {
                            val childNode = childNodeList.item(childNodeIndex)
                            val nodeName = childNode.nodeName
                            if (nodeName == JxlMappingColumnBean.TAG_COLUMN) {
                                val jxlMappingColumnBean = JxlMappingColumnBean()
                                val childNodeAttributeMap = childNode.attributes
                                JavaXmlUtil.initializeFromAttributeMap(jxlMappingColumnBean, childNodeAttributeMap)
                                jxlMappingBean.addJxlMappingColumnBean(jxlMappingColumnBean)
                            }
                        }
                    }
                    val useFor = jxlMappingBean.useFor
                    val type = jxlMappingBean.type
                    if (useFor == JxlMappingBean.USE_FOR_IMPORT) {
                        typeImportJxlMappingBeanMap[type] = jxlMappingBean
                        nameImportJxlMappingBeanMap[this.classLoader.loadClass(type).simpleName] = jxlMappingBean
                    } else if (useFor == JxlMappingBean.USE_FOR_EXPORT) {
                        typeExportJxlMappingBeanMap[type] = jxlMappingBean
                        nameExportJxlMappingBeanMap[this.classLoader.loadClass(type).simpleName] = jxlMappingBean
                    }
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
        typeImportJxlMappingBeanMap.clear()
        nameImportJxlMappingBeanMap.clear()
        typeExportJxlMappingBeanMap.clear()
        nameExportJxlMappingBeanMap.clear()
    }

    /**
     * findImportJxlMappingBean
     * @param <T>
     * @param clazz
     * @return JxlMappingBean
    </T> */
    fun <T : Any> findImportJxlMappingBean(clazz: Class<T>?): JxlMappingBean? {
        var bean: JxlMappingBean? = null
        if (clazz != null) {
            val className = clazz.name
            bean = typeImportJxlMappingBeanMap[className]
        }
        return bean
    }

    /**
     * @param name full name or simple name
     * @return JxlMappingBean
     * @throws Exception
     */
    @Throws(Exception::class)
    fun findImportJxlMappingBean(name: String?): JxlMappingBean? {
        var bean: JxlMappingBean? = null
        if (name != null) {
            bean = typeImportJxlMappingBeanMap[name]
            if (bean == null) {
                bean = nameImportJxlMappingBeanMap[name]
            }
        }
        return bean
    }

    /**
     * findExportJxlMappingBean
     * @param <T>
     * @param clazz
     * @return JxlMappingBean
    </T> */
    fun <T : Any> findExportJxlMappingBean(clazz: Class<T>?): JxlMappingBean? {
        var bean: JxlMappingBean? = null
        if (clazz != null) {
            val className = clazz.name
            bean = typeExportJxlMappingBeanMap[className]
        }
        return bean
    }

    /**
     * @param name full name or simple name
     * @return JxlMappingBean
     * @throws Exception
     */
    @Throws(Exception::class)
    fun findExportJxlMappingBean(name: String?): JxlMappingBean? {
        var bean: JxlMappingBean? = null
        if (name != null) {
            bean = typeExportJxlMappingBeanMap[name]
            if (bean == null) {
                bean = nameExportJxlMappingBeanMap[name]
            }
        }
        return bean
    }
}
