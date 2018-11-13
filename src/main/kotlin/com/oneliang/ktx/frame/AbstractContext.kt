package com.oneliang.ktx.frame

import com.oneliang.ktx.util.jar.JarClassLoader

abstract class AbstractContext : Context {
    companion object {
        internal val objectMap = mutableMapOf<String, Any>()
        internal var jarClassLoader = JarClassLoader(Thread.currentThread().contextClassLoader)
    }

    var classesRealPath: String? = null
    var projectRealPath: String? = null

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
