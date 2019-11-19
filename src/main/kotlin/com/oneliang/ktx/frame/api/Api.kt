package com.oneliang.ktx.frame.api

import com.oneliang.ktx.Constants

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class Api {

    @MustBeDocumented
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Document(val key: String, val inputObjectKey: String = Constants.String.BLANK, val outputObjectKey: String = Constants.String.BLANK)

    @MustBeDocumented
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class DocumentObjectMap
}