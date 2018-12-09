package com.oneliang.ktx.frame.uri

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.util.common.JavaXmlUtil
import java.util.concurrent.ConcurrentHashMap

class UriMappingContext : AbstractContext() {
    companion object {
        private const val REGEX = "\\{[\\w]*\\}"
        private const val FIRST_REGEX = "\\{"
        internal val uriMappingBeanMap: MutableMap<String, UriMappingBean> = ConcurrentHashMap()

        /**
         * find uri to
         * @param uriFrom
         * @return String
         */
        fun findUriTo(uriFrom: String): String {
            var uriTo: String = Constants.String.BLANK
            run loop@{
                uriMappingBeanMap.forEach { (from, uriMappingBean) ->
                    val fromRegex = Constants.Symbol.XOR + from + Constants.Symbol.DOLLAR
                    if (uriFrom.matches(fromRegex.toRegex())) {
                        uriTo = uriMappingBean.to
                        uriTo = uriFrom.replace(fromRegex.toRegex(), uriTo)
                        return@loop
                    }
                }
            }
            return uriTo
        }
    }

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
            val uriBeanElementList = root.getElementsByTagName(UriMappingBean.TAG_URI)
            if (uriBeanElementList != null) {
                val length = uriBeanElementList.length
                for (index in 0 until length) {
                    val beanElement = uriBeanElementList.item(index)
                    val uriMappingBean = UriMappingBean()
                    val attributeMap = beanElement.attributes
                    JavaXmlUtil.initializeFromAttributeMap(uriMappingBean, attributeMap)
                    uriMappingBeanMap[uriMappingBean.from] = uriMappingBean
                }
            }
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }
    }

    override fun destroy() {
        uriMappingBeanMap.clear()
    }
}
