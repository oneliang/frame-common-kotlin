package com.oneliang.ktx.frame.ioc.aop

import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * annotation DataCache for the method of no arguments
 * @author Dandelion
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
annotation class DataCache(val updateTime: Long = 10000)
