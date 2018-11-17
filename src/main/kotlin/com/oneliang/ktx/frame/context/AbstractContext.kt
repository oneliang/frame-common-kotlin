package com.oneliang.ktx.frame.context

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.jar.JarClassLoader

abstract class AbstractContext : Context {
    companion object {
        internal val objectMap = mutableMapOf<String, Any>()
        internal var jarClassLoader = JarClassLoader(Thread.currentThread().contextClassLoader)
    }

    var classesRealPath: String = Constants.String.BLANK
    var projectRealPath: String = Constants.String.BLANK

    protected var classLoader: ClassLoader = Thread.currentThread().contextClassLoader

    /**
     * find bean
     *
     * @param id
     * @return T
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> findBean(id: String): T? {
        return objectMap[id] as T
    }
}
