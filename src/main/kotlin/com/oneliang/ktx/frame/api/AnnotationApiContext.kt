package com.oneliang.ktx.frame.api

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.frame.context.AnnotationContextUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

class AnnotationApiContext : AbstractContext() {
    companion object {
        private val logger = LoggerManager.getLogger(AnnotationApiContext::class)
        internal val apiClassList = CopyOnWriteArrayList<KClass<*>>()
    }

    override fun initialize(parameters: String) {
        try {
            val classList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(parameters, classLoader, classesRealPath, jarClassLoader, Api::class)
            apiClassList += classList
            for (clazz in apiClassList) {
                logger.info(clazz)
            }
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }
    }

    override fun destroy() {
        apiClassList.clear()
    }
}