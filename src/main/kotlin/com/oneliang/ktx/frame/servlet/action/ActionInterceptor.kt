package com.oneliang.ktx.frame.servlet.action

import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import com.oneliang.util.common.StringUtil

@Documented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(RetentionPolicy.RUNTIME)
annotation class ActionInterceptor(val id: String = StringUtil.BLANK, val mode: Mode = Mode.SINGLE_ACTION) {

    enum class Mode {
        GLOBAL_ACTION_BEFORE, GLOBAL_ACTION_AFTER, SINGLE_ACTION
    }
}
