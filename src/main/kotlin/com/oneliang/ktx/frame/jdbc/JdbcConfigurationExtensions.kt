package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.frame.configuration.ConfigurationContext
import kotlin.reflect.KClass

/**
 * find mappingBean
 *
 * @param <T>
 * @param clazz
 * @return MappingBean
</T> */
fun <T : Any> ConfigurationContext.findMappingBean(clazz: KClass<T>): MappingBean? {
    val mappingContext = this.findContext(MappingContext::class)
    return mappingContext?.findMappingBean(clazz)
}

/**
 * find mappingBean
 *
 * @param name
 * @return MappingBean
 */
fun ConfigurationContext.findMappingBean(name: String): MappingBean? {
    val mappingContext = this.findContext(MappingContext::class)
    return mappingContext?.findMappingBean(name)
}