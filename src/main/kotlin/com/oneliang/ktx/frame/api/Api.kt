package com.oneliang.ktx.frame.api

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class Api(val requestMapping: String, val mode: Mode = Mode.REQUEST) {
    enum class Mode {
        REQUEST, RESPONSE
    }
}