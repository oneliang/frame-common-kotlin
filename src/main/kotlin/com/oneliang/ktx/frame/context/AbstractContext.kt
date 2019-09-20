package com.oneliang.ktx.frame.context

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.jar.JarClassLoader
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractContext : Context {
    companion object {
        internal val objectMap = ConcurrentHashMap<String, Any>()
        internal var jarClassLoader = JarClassLoader(Thread.currentThread().contextClassLoader)
    }

    internal var classLoader: ClassLoader = Thread.currentThread().contextClassLoader
        set(value) {
            field = value
            classesRealPath = field.getResource(Constants.String.BLANK)?.path.nullToBlank()
        }

    var classesRealPath: String = this.classLoader.getResource(Constants.String.BLANK)?.path.nullToBlank()
        set(value) {
            if (value.isNotBlank()) {
                field = value
            } else {
                throw RuntimeException("classesRealPath can not be blank.")
            }
        }

    var projectRealPath: String = Constants.String.BLANK

    /**
     * find bean
     *
     * @param id
     * @return T
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> findBean(id: String): T? {
        return objectMap[id] as T?
    }
}
