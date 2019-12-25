package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants

private fun <T> sqlConditionTransform(instance: T): String {
    return Constants.Symbol.SINGLE_QUOTES + instance.toString() + Constants.Symbol.SINGLE_QUOTES
}

fun <T, R> Iterable<T>.toSqlCondition(transform: (T) -> R): String {
    return this.joinToString {
        sqlConditionTransform(transform(it))
    }
}

fun <T, R> Array<T>.toSqlCondition(transform: (T) -> R): String {
    return this.joinToString {
        sqlConditionTransform(transform(it))
    }
}