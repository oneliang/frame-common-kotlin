package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.toFormatString
import java.util.*
import kotlin.reflect.KClass


/**
 * mostly for mysql database
 * @author Dandelion
 * @since 2011-01-07
 */
class DefaultSqlProcessor : AbstractSqlProcessor() {

    /**
     * mostly for mysql database
     * default sql insert processor
     * promiss:if value is null return null,if it is not null return the new value
     */
    override fun <T : Any> beforeInsertProcess(clazz: KClass<T>, value: Any?): String {
        return if (value != null) {
            when (clazz) {
                Boolean::class -> value.toString()
                Date::class -> "'" + (value as Date).toFormatString() + "'"
                else -> "'" + value.toString() + "'"
            }
        } else {
            Constants.String.NULL
        }
    }

    /**
     * mostly for mysql database
     * default sql update processor
     * promiss:if value is null,return the blank,if it is not null return the new value
     */
    override fun <T : Any> beforeUpdateProcess(clazz: KClass<T>, isId: Boolean, columnName: String, value: Any?): String {
        return if (isId) {
            " AND $columnName='$value'"
        } else {
            if (value != null) {
                when (clazz) {
                    Boolean::class -> "$columnName=$value,"
                    Date::class -> columnName + "='" + (value as Date).toFormatString() + "',"
                    else -> "$columnName='$value',"
                }
            } else {
                Constants.String.BLANK
            }
        }
    }

    /**
     * before delete process,for generate delete sql
     * @param <T>
     * @param clazz
     * @param isId
     * @param columnName
     * @param value
     * @return String
    </T> */
    override fun <T : Any> beforeDeleteProcess(clazz: KClass<T>, isId: Boolean, columnName: String, value: Any?): String {
        return if (isId) {
            " AND $columnName='$value'"
        } else {
            if (value != null) {
                when (clazz) {
                    Boolean::class -> " AND $columnName=$value"
                    Date::class -> " AND " + columnName + "='" + (value as Date).toFormatString() + "'"
                    else -> " AND $columnName='$value'"
                }
            } else {
                Constants.String.BLANK
            }
        }
    }
}