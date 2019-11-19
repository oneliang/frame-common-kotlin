package com.oneliang.ktx.frame.api

import com.oneliang.ktx.Constants

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class Api(val requestMapping: String, val mode: Mode = Mode.REQUEST) {
    companion object {
        const val DEFAULT_URI = Constants.Symbol.WILDCARD
    }

    enum class Mode {
        REQUEST, RESPONSE, RESPONSE_DATA
    }
}