package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants

private fun <T> defaultSqlConditionTransform(instance: T): String {
    return when (instance) {
        is Int, Long -> instance.toString()
        else ->
            Constants.Symbol.SINGLE_QUOTES + instance + Constants.Symbol.SINGLE_QUOTES

    }
}

fun <T> Iterable<T>.toSqlCondition(transform: (T) -> String = ::defaultSqlConditionTransform): String {
    return this.joinToString {
        transform(it)
    }
}

fun <T> Array<T>.toSqlCondition(transform: (T) -> String = ::defaultSqlConditionTransform): String {
    return this.joinToString {
        transform(it)
    }
}