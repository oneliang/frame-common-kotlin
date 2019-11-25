package com.oneliang.ktx.frame.api

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.frame.context.AnnotationContextUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

class AnnotationApiContext : AbstractContext() {
    companion object {
        private val logger = LoggerManager.getLogger(AnnotationApiContext::class)
        internal val apiClassList = CopyOnWriteArrayList<KClass<*>>()
        internal val apiDocumentObjectMap = ConcurrentHashMap<String, Any>()
    }

    override fun initialize(parameters: String) {
        try {
            val kClassList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(parameters, classLoader, classesRealPath, jarClassLoader, Api::class)
            apiClassList += kClassList
            for (kClass in apiClassList) {
                logger.info(kClass.toString())
            }
            val apiDocumentObjectMapClassList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(parameters, classLoader, classesRealPath, jarClassLoader, Api.DocumentObjectMap::class)
            for (apiDocumentObjectMapClass in apiDocumentObjectMapClassList) {
                val apiDocumentObjectMapInstance = apiDocumentObjectMapClass.java.newInstance()
                if (apiDocumentObjectMapInstance is ApiDocumentObjectMap) {
                    val instanceObjectMap = apiDocumentObjectMapInstance.generateApiDocumentObjectMap()
                    apiDocumentObjectMap += instanceObjectMap
                }
            }
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }
    }

    override fun destroy() {
        apiClassList.clear()
        apiDocumentObjectMap.clear()
    }
}