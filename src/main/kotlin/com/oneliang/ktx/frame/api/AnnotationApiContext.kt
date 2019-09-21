package com.oneliang.ktx.frame.api

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.frame.context.AnnotationContextUtil
import com.oneliang.ktx.util.logging.LoggerManager

class AnnotationApiContext : AbstractContext() {
    companion object {
        private val logger = LoggerManager.getLogger(AnnotationApiContext::class)
    }

    override fun initialize(parameters: String) {
        try {
            val classList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(parameters, classLoader, classesRealPath, jarClassLoader, Api::class)
            for (clazz in classList) {
                logger.info(clazz)
            }
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }
    }

    override fun destroy() {
    }
}