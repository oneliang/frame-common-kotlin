package com.oneliang.ktx.frame.uri

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.util.common.JavaXmlUtil
import com.oneliang.ktx.util.common.parseRegexGroup
import com.oneliang.ktx.util.common.parseStringGroup
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
            val iterator = uriMappingBeanMap.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val from = entry.key
                val fromRegex = Constants.Symbol.XOR + from + Constants.Symbol.DOLLAR
                if (uriFrom.matches(fromRegex.toRegex())) {
                    uriTo = entry.value.to
                    val groupList = uriFrom.parseRegexGroup(fromRegex)
                    val parameterList = uriTo.parseStringGroup(REGEX, FIRST_REGEX, Constants.String.BLANK, 1)
                    for (parameter in parameterList) {
                        uriTo = uriTo.replaceFirst(REGEX.toRegex(), groupList[Integer.parseInt(parameter)])
                    }
                    break
                }
            }
            return uriTo
        }
    }

    override fun initialize(parameters: String) {
        try {
            var path = parameters
            val tempClassesRealPath = if (classesRealPath.isBlank()) {
                this.classLoader.getResource(Constants.String.BLANK).getPath()
            } else {
                classesRealPath
            }
            path = tempClassesRealPath!! + path
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
