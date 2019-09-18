package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.ObjectUtil
import java.util.*
import kotlin.reflect.KClass

/**
 * sql util for generate the common sql such as:select,insert,delete,update
 * @author Dandelion
 * @since 2008-09-25
 */
object SqlInjectUtil {

    /**
     * Method: for database use,make the insert sql stringparameterList
     * @param <T>
     * @param instance
     * @param table
     * @param mappingBean
     * @return Pair<String,List<Any>>
    </T> */
    fun <T : Any> objectToInsertSql(instance: T, table: String, mappingBean: MappingBean): Pair<String, List<Any>> {
        val sql = StringBuilder()
        val parameterList = mutableListOf<Any>()
        try {
            val methods = instance.javaClass.methods
            val columnNameStringBuilder = StringBuilder()
            val values = StringBuilder()
            for (method in methods) {
                val methodName = method.name
                val fieldName = ObjectUtil.methodNameToFieldName(methodName)
                if (fieldName.isBlank()) {
                    continue
                }
                val columnName = mappingBean.getColumn(fieldName)
                if (columnName.isBlank()) {
                    continue
                }
                columnNameStringBuilder.append(columnName + Constants.Symbol.COMMA)
                val value = method.invoke(instance)
                values.append(Constants.Symbol.QUESTION_MARK + Constants.Symbol.COMMA)
                parameterList.add(value)
            }
            val tempTable = if (table.isBlank()) {
                mappingBean.table
            } else {
                table
            }
            sql.append("INSERT INTO ")
            sql.append(tempTable)
            sql.append("(" + columnNameStringBuilder.substring(0, columnNameStringBuilder.length - 1) + ")")
            sql.append(" VALUES (" + values.substring(0, values.length - 1) + ")")
        } catch (e: Exception) {
            throw SqlInjectUtilException(e)
        }
        return sql.toString() to parameterList
    }

    /**
     * Method: for database use,make the insert sql string
     * @param <T>
     * @param clazz
     * @param table
     * @param mappingBean
     * @return Pair<String, List<String>>
    </T> */
    fun <T : Any> classToInsertSql(clazz: KClass<T>, table: String, mappingBean: MappingBean): Pair<String, List<String>> {
        val sql = StringBuilder()
        val fieldNameList = mutableListOf<String>()
        val methods = clazz.java.methods
        val columnNameStringBuilder = StringBuilder()
        val valueStringBuilder = StringBuilder()
        for (method in methods) {
            val methodName = method.name
            val fieldName = ObjectUtil.methodNameToFieldName(methodName)
            if (fieldName.isBlank()) {
                continue
            }
            val columnName = mappingBean.getColumn(fieldName)
            if (columnName.isBlank()) {
                continue
            }
            columnNameStringBuilder.append(columnName + Constants.Symbol.COMMA)
            valueStringBuilder.append(Constants.Symbol.QUESTION_MARK + Constants.Symbol.COMMA)
            fieldNameList.add(fieldName)
        }
        val tempTable = if (table.isBlank()) {
            mappingBean.table
        } else {
            table
        }
        sql.append("INSERT INTO ")
        sql.append(tempTable)
        sql.append("(" + columnNameStringBuilder.substring(0, columnNameStringBuilder.length - 1) + ")")
        sql.append(" VALUES (" + valueStringBuilder.substring(0, valueStringBuilder.length - 1) + ")")
        return sql.toString() to fieldNameList
    }

    /**
     * Method: for database use,make the update sql string
     * @param <T>
     * @param instance
     * @param table
     * @param otherCondition
     * @param byId
     * @param mappingBean
     * @return Pair<String, List<Any>>
    </T> */
    fun <T : Any> objectToUpdateSql(instance: T, table: String, otherCondition: String, byId: Boolean, mappingBean: MappingBean): Pair<String, List<Any>> {
        val sql = StringBuilder()
        val idList = mutableListOf<Any>()
        val valueList = mutableListOf<Any>()
        val parameterList = mutableListOf<Any>()
        try {
            val methods = instance.javaClass.methods
            val columnsAndValues = StringBuilder()
            val condition = StringBuilder()
            for (method in methods) {
                val methodName = method.name
                val fieldName = ObjectUtil.methodNameToFieldName(methodName)
                if (fieldName.isBlank()) {
                    continue
                }
                val columnName = mappingBean.getColumn(fieldName)
                if (columnName.isBlank()) {
                    continue
                }
                val isId = mappingBean.isId(fieldName)
                val value = method.invoke(instance)
                if (byId && isId) {
                    val result: String
                    if (value != null) {
                        result = " AND $columnName=?"
                        idList.add(value)
                        condition.append(result)
                    } else {
                        result = " AND " + columnName + " is " + Constants.String.NULL
                        condition.append(result)
                    }
                } else {
                    if (value != null) {
                        val result = "$columnName=?,"
                        valueList.add(value)
                        columnsAndValues.append(result)
                    }
                }
            }
            val tempTable = if (table.isBlank()) {
                mappingBean.table
            } else {
                table
            }
            sql.append("UPDATE ")
            sql.append(tempTable)
            sql.append(" SET " + columnsAndValues.substring(0, columnsAndValues.length - 1))
            sql.append(" WHERE 1=1 $condition $otherCondition")
            parameterList.addAll(valueList)
            parameterList.addAll(idList)
        } catch (e: Exception) {
            throw SqlInjectUtilException(e)
        }
        return sql.toString() to parameterList
    }

    /**
     * Method: for database use,make the update sql string
     * @param <T>
     * @param clazz
     * @param table
     * @param otherCondition
     * @param byId
     * @param mappingBean
     * @return Pair<String, List<String>>
    </T> */
    fun <T : Any> classToUpdateSql(clazz: KClass<T>, table: String, otherCondition: String, byId: Boolean, mappingBean: MappingBean): Pair<String, List<String>> {
        val sql = StringBuilder()
        val fieldNameList = mutableListOf<String>()
        val idList = ArrayList<String>()
        val valueList = ArrayList<String>()
        val methods = clazz.java.methods
        val columnsAndValues = StringBuilder()
        val condition = StringBuilder()
        for (method in methods) {
            val methodName = method.name
            val fieldName = ObjectUtil.methodNameToFieldName(methodName)
            if (fieldName.isBlank()) {
                continue
            }
            val columnName = mappingBean.getColumn(fieldName)
            if (columnName.isBlank()) {
                continue
            }
            val isId = mappingBean.isId(fieldName)
            val result: String
            if (byId && isId) {
                result = " AND $columnName=?"
                idList.add(fieldName)
                condition.append(result)
            } else {
                result = "$columnName=?,"
                valueList.add(fieldName)
                columnsAndValues.append(result)
            }
        }
        val tempTable = if (table.isBlank()) {
            mappingBean.table
        } else {
            table
        }
        sql.append("UPDATE ")
        sql.append(tempTable)
        sql.append(" SET " + columnsAndValues.substring(0, columnsAndValues.length - 1))
        sql.append(" WHERE 1=1 $condition $otherCondition")
        fieldNameList.addAll(valueList)
        fieldNameList.addAll(idList)
        return sql.toString() to fieldNameList
    }

    /**
     * Method: for database use make the delete sql string,can delete one row
     * not the many rows
     * @param <T>
     * @param instance
     * @param table
     * @param otherCondition
     * @param byId
     * @param mappingBean
     * @return Pair<String,List<Any>>
    </T> */
    fun <T : Any> objectToDeleteSql(instance: T, table: String, otherCondition: String, byId: Boolean, mappingBean: MappingBean): Pair<String, List<Any>> {
        val sql: String
        val parameterList = mutableListOf<Any>()
        try {
            val methods = instance.javaClass.methods
            val condition = StringBuilder()
            for (method in methods) {
                val methodName = method.name
                val fieldName = ObjectUtil.methodNameToFieldName(methodName)
                if (fieldName.isBlank()) {
                    continue
                }
                val columnName = mappingBean.getColumn(fieldName)
                if (columnName.isBlank()) {
                    continue
                }
                val isId = mappingBean.isId(fieldName)
                if ((byId && isId) || (!byId && !isId)) {
                    val value = method.invoke(instance)
                    if (value != null) {
                        condition.append(" AND $columnName=?")
                        parameterList.add(value)
                    } else {
                        if (byId && isId) {
                            condition.append(" AND " + columnName + " is " + Constants.String.NULL)
                        }
                    }
                }
            }
            val tempTable = if (table.isBlank()) {
                mappingBean.table
            } else {
                table
            }
            sql = SqlUtil.deleteSql(tempTable, "$condition $otherCondition")
        } catch (e: Exception) {
            throw SqlInjectUtilException(e)
        }
        return sql to parameterList
    }

    /**
     * Method: for database use make the delete sql string,sql binding
     * @param <T>
     * @param clazz
     * @param table
     * @param otherCondition
     * @param byId
     * @param mappingBean
     * @return Pair<String,List<String>>
    </T> */
    fun <T : Any> classToDeleteSql(clazz: KClass<T>, table: String, otherCondition: String, byId: Boolean, mappingBean: MappingBean): Pair<String, List<String>> {
        val methods = clazz.java.methods
        val fieldNameList = mutableListOf<String>()
        val condition = StringBuilder()
        for (method in methods) {
            val methodName = method.name
            val fieldName = ObjectUtil.methodNameToFieldName(methodName)
            if (fieldName.isBlank()) {
                continue
            }

            val columnName = mappingBean.getColumn(fieldName)
            if (columnName.isBlank()) {
                continue
            }
            val isId = mappingBean.isId(fieldName)
            if (byId && isId || !byId && !isId) {
                condition.append(" AND $columnName=?")
                fieldNameList.add(fieldName)
            }
        }
        val tempTable = if (table.isBlank()) {
            mappingBean.table
        } else {
            table
        }
        val sql = SqlUtil.deleteSql(tempTable, "$condition $otherCondition")
        return sql to fieldNameList
    }

    class SqlInjectUtilException(cause: Throwable) : RuntimeException(cause)
}
