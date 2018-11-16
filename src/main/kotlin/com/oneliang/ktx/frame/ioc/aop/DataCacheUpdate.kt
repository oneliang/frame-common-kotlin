package com.oneliang.ktx.frame.ioc.aop

import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * annotation DataCacheUpdate for the method of the last arguments which is data cache
 * @author Dandelion
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
annotation class DataCacheUpdate(val dataCacheMethod: String)
