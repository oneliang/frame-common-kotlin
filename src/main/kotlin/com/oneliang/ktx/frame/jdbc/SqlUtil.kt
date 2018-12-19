package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.MappingNotFoundException
import com.oneliang.ktx.util.common.ObjectUtil
import com.oneliang.ktx.util.json.toJson
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass

/**
 * sql util for generate the common sql such as:select,insert,delete,update
 * @author Dandelion
 * @since 2008-09-25
 */
object SqlUtil {

    /**
     *
     *
     * Method: for database use,ResultSet to object list
     *
     * @param <T>
     * @param resultSet
     * @param clazz
     * @param mappingBean
     * @return List<T>
    </T></T> */
    fun <T : Any> resultSetToObjectList(resultSet: ResultSet, clazz: KClass<T>, mappingBean: MappingBean, sqlProcessor: SqlProcessor): List<T> {
        val list = mutableListOf<T>()
        try {
            var instance: T? = null
            //			Field[] field = clazz.getDeclaredFields();// get the fields one
            val methods = clazz.java.methods
            // time is ok
            while (resultSet.next()) {
                instance = clazz.java.newInstance()// more instance
                for (method in methods) {
                    val methodName = method.name
                    val fieldName = if (methodName.startsWith(Constants.Method.PREFIX_SET)) {
                        ObjectUtil.methodNameToFieldName(Constants.Method.PREFIX_SET, methodName)
                    } else {
                        Constants.String.BLANK
                    }
                    if (fieldName.isBlank()) {
                        continue
                    }
                    val columnName = mappingBean.getColumn(fieldName)
                    if (columnName.isBlank()) {
                        continue
                    }
                    val classes = method.parameterTypes
                    var value: Any?
                    if (classes.size == 1) {
                        value = sqlProcessor.afterSelectProcess(classes[0].kotlin, resultSet, columnName)
                        method.invoke(instance, value)
                    }
                }
                list.add(instance)
            }
        } catch (e: Exception) {
            throw SqlUtilException(e)
        }
        return list
    }

    /**
     *
     * Method: the simple select sql
     * @param columns can be null
     * @param table can not be null
     * @param condition can be null
     * @return String
     */
    fun selectSql(columns: Array<String> = emptyArray(), table: String, condition: String = Constants.String.BLANK): String {
        var selectColumn = StringBuilder()
        if (columns.isNotEmpty()) {
            for (column in columns) {
                selectColumn.append(column + Constants.Symbol.COMMA)
            }
            selectColumn = selectColumn.delete(selectColumn.length - 1, selectColumn.length)
        } else {
            selectColumn.append(Constants.Symbol.WILDCARD)
        }
        val sql = StringBuilder()
        sql.append("SELECT ")
        sql.append(selectColumn)
        sql.append(" FROM ")
        sql.append(table)
        sql.append(" WHERE 1=1 ")
        sql.append(condition)
        return sql.toString()
    }

    /**
     *
     * Method: class to select sql
     * @param <T>
     * @param columns String[] which columns do you select
     * @param table
     * @param condition
     * @param mappingBean
     * @return String
    </T> */
    fun <T : Any> selectSql(columns: Array<String>, table: String, condition: String = Constants.String.BLANK, mappingBean: MappingBean?): String {
        val sql: String
        if (table.isNotBlank() || mappingBean != null) {
            val tempTable = if (table.isBlank() && mappingBean != null) {
                mappingBean.table
            } else {
                table
            }
            sql = selectSql(columns, tempTable, condition)
        } else {
            throw MappingNotFoundException("Can not find the object mapping or table,object mapping or table can not be null or empty string!")
        }
        return sql
    }

    /**
     * delete sql
     * @param table can not be null
     * @param condition
     * @return String
     */
    fun deleteSql(table: String, condition: String = Constants.String.BLANK): String {
        val sql = StringBuilder()
        sql.append("DELETE FROM ")
        sql.append(table)
        sql.append(" WHERE 1=1 ")
        sql.append(condition)
        return sql.toString()
    }

    /**
     * delete sql
     * @param table
     * @param condition
     * @param mappingBean
     * @return String
     */
    fun deleteSql(table: String, condition: String = Constants.String.BLANK, mappingBean: MappingBean?): String {
        val sql: String
        if (table.isNotBlank() || mappingBean != null) {
            val tempTable = if (table.isBlank() && mappingBean != null) {
                mappingBean.table
            } else {
                table
            }
            sql = deleteSql(tempTable, condition)
        } else {
            throw MappingNotFoundException("Can not find the object or table,object or table can not be null or empty string!")
        }
        return sql
    }

    /**
     *
     * Method: class to select sql with id
     * @param <T>
     * @param clazz
     * @param mappingBean
     * @return String
    </T> */
    fun <T : Any> classToSelectIdSql(clazz: KClass<T>, mappingBean: MappingBean): String {
        val methods = clazz.java.methods
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
            if (!isId) {
                continue
            }
            condition.append(" AND $columnName=?")
        }
        val table = mappingBean.table
        return selectSql(emptyArray(), table, condition.toString())
    }

    /**
     *
     *
     * Method: for database use make the delete sql string,can delete one row
     * not the many rows,support single id
     *
     * @param <T>
     * @param clazz
     * @param id
     * @param mappingBean
     * @return String
    </T> */
    fun <T : Any, IdType : Any> classToDeleteOneRowSql(clazz: KClass<T>, id: IdType, mappingBean: MappingBean): String {
        return classToDeleteSql(clazz, arrayOf<Any>(id), mappingBean, DeleteType.ONE_ROW)
    }

    /**
     *
     *
     * Method: for database use make the delete sql string,can delete multiple row
     *
     * @param <T>
     * @param clazz
     * @param ids
     * @param mappingBean
     * @return String
    </T> */
    fun <T : Any, IdType : Any> classToDeleteMultipleRowSql(clazz: KClass<T>, ids: Array<IdType>, mappingBean: MappingBean): String {
        return classToDeleteSql(clazz, ids, mappingBean, DeleteType.MULTIPLE_ROW)
    }

    /**
     *
     *
     * Method: for database use make the delete sql string,can delete one row and multi row
     *
     * @param <T>
     * @param clazz
     * @param ids
     * @param mappingBean
     * @param deleteType
     * @return String
    </T> */
    private fun <T : Any, IdType : Any> classToDeleteSql(clazz: KClass<T>, ids: Array<IdType>, mappingBean: MappingBean, deleteType: DeleteType): String {
        val methods = clazz.java.methods
        val condition = StringBuilder()
        for (method in methods) {
            val methodName = method.name
            val fieldName = ObjectUtil.methodNameToFieldName(methodName)
            if (fieldName.isBlank()) {
                continue
            }
            val isId = mappingBean.isId(fieldName)
            val columnName = mappingBean.getColumn(fieldName)
            if (columnName.isBlank()) {
                continue
            }
            if (isId) {
                if (ids.isEmpty()) {
                    throw NullPointerException("ids can not be null or empty.")
                }
                when (deleteType) {
                    SqlUtil.DeleteType.ONE_ROW -> condition.append(" AND " + columnName + "='" + ids[0] + "'")
                    SqlUtil.DeleteType.MULTIPLE_ROW -> {
                        var id = ids.toJson()
                        id = id.replace(("^\\" + Constants.Symbol.MIDDLE_BRACKET_LEFT), "").replace(("\\" + Constants.Symbol.MIDDLE_BRACKET_RIGHT + "$"), "")
                        condition.append(" AND $columnName IN ($id)")
                    }
                }
            }
        }
        val table = mappingBean.table
        return deleteSql(table, condition.toString())
    }

    /**
     *
     *
     * Method: object to select sql
     *
     * @param <T>
     * @param instance
     * @param table
     * @param mappingBean
     * @return String
    </T> */
    fun <T : Any> objectToSelectSql(instance: T, table: String, mappingBean: MappingBean): String {
        val sql: String
        try {
            val methods = instance.javaClass.methods
            val condition = StringBuilder()
            for (method in methods) {
                val methodName = method.name
                val fieldName = ObjectUtil.methodNameToFieldName(methodName)
                if (fieldName.isBlank()) {
                    continue
                }
                val isId = mappingBean.isId(fieldName)
                val columnName = mappingBean.getColumn(fieldName)
                if (columnName.isBlank()) {
                    continue
                }
                if (!isId) {
                    continue
                }
                val value = method.invoke(instance)
                condition.append(" AND $columnName='$value'")
            }
            val tempTable = if (table.isBlank()) {
                mappingBean.table
            } else {
                table
            }
            sql = selectSql(emptyArray(), tempTable, condition.toString())
        } catch (e: Exception) {
            throw SqlUtilException(e)
        }
        return sql
    }

    /**
     *
     *
     * Method: for database use,make the update sql string
     *
     * @param <T>
     * @param instance
     * @param table
     * @param mappingBean
     * @param sqlProcessor
     * @return String
    </T> */
    fun <T : Any> objectToInsertSql(instance: T, table: String, mappingBean: MappingBean, sqlProcessor: SqlProcessor? = null): String {
        val sql: String
        try {
            val methods = instance.javaClass.methods
            val columnNames = StringBuilder()
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
                columnNames.append(columnName + Constants.Symbol.COMMA)
                val type = method.returnType
                val value = method.invoke(instance)
                if (sqlProcessor != null) {
                    values.append(sqlProcessor.beforeInsertProcess(type.kotlin, value) + Constants.Symbol.COMMA)
                } else {
                    if (value != null) {
                        values.append("'" + value.toString() + "'")
                        values.append(Constants.Symbol.COMMA)
                    } else {
                        values.append(Constants.String.NULL + Constants.Symbol.COMMA)
                    }
                }
            }
            val tempTable = if (table.isBlank()) {
                mappingBean.table
            } else {
                table
            }
            val stringBuilder = StringBuilder()
            stringBuilder.append("INSERT INTO ")
            stringBuilder.append(tempTable)
            stringBuilder.append("(" + columnNames.substring(0, columnNames.length - 1) + ")")
            stringBuilder.append(" VALUES (" + values.substring(0, values.length - 1) + ")")
            sql = stringBuilder.toString()
        } catch (e: Exception) {
            throw SqlUtilException(e)
        }
        return sql
    }

    /**
     *
     *
     * Method: for database use,make the update sql string
     *
     * @param <T>
     * @param instance
     * @param table
     * @param otherCondition
     * @param byId
     * @param mappingBean
     * @param sqlProcessor
     * @return String
    </T> */
    fun <T : Any> objectToUpdateSql(instance: T, table: String, otherCondition: String, byId: Boolean, mappingBean: MappingBean, sqlProcessor: SqlProcessor? = null): String {
        val sql: String
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
                val type = method.returnType
                val value = method.invoke(instance)
                val result = sqlProcessor?.beforeUpdateProcess(type.kotlin, isId, columnName, value) ?: if (value != null) {
                    "$columnName='$value',"
                } else {
                    Constants.String.BLANK
                }
                if (isId) {
                    if (byId) {
                        condition.append(result)
                    }
                } else {
                    columnsAndValues.append(result)
                }
            }
            val tempTable = if (table.isBlank()) {
                mappingBean.table
            } else {
                table
            }
            val stringBuilder = StringBuilder()
            stringBuilder.append("UPDATE ")
            stringBuilder.append(tempTable)
            stringBuilder.append(" SET " + columnsAndValues.substring(0, columnsAndValues.length - 1))
            stringBuilder.append(" WHERE 1=1 $condition $otherCondition")
            sql = stringBuilder.toString()
        } catch (e: Exception) {
            throw SqlUtilException(e)
        }
        return sql
    }

    /**
     *
     *
     * Method: for database use make the delete sql string,can delete one row
     * not the many rows
     *
     *
     * @param <T>
     * @param instance
     * @param table
     * @param otherCondition
     * @param mappingBean
     * @return String
    </T> */
    fun <T : Any> objectToDeleteSql(instance: T, table: String, otherCondition: String, byId: Boolean, mappingBean: MappingBean, sqlProcessor: SqlProcessor? = null): String {
        val sql: String
        try {
            val methods = instance.javaClass.methods
            val condition = StringBuilder()
            for (method in methods) {
                val methodName = method.name
                val fieldName = ObjectUtil.methodNameToFieldName(methodName)
                if (fieldName.isBlank()) {
                    continue
                }
                val isId = mappingBean.isId(fieldName)
                val columnName = mappingBean.getColumn(fieldName)
                if (columnName.isBlank()) {
                    continue
                }
                if (byId && isId || !byId && !isId) {
                    val type = method.returnType
                    val value = method.invoke(instance)
                    if (sqlProcessor != null) {
                        val result = sqlProcessor.beforeDeleteProcess(type.kotlin, isId, columnName, value)
                        condition.append(result)
                    } else {
                        if (value != null) {
                            condition.append(" AND $columnName='$value'")
                        }
                    }
                }
            }
            val tempTable = if (table.isBlank()) {
                mappingBean.table
            } else {
                table
            }
            sql = deleteSql(tempTable, condition.toString() + " " + otherCondition)
        } catch (e: Exception) {
            throw SqlUtilException(e)
        }
        return sql
    }

    /**
     * create table sqls,include drop table
     * @param annotationMappingBean
     * @return String[]
     */
    fun createTableSqls(annotationMappingBean: AnnotationMappingBean): Array<String> {
        val sqlList = mutableListOf<String>()
        val table = annotationMappingBean.table
        if (annotationMappingBean.isDropIfExist) {
            sqlList.add("DROP TABLE IF EXISTS " + table + Constants.Symbol.SEMICOLON)
        }
        val createTableSql = StringBuilder()
        createTableSql.append("CREATE TABLE $table (")
        val mappingColumnBeanList = annotationMappingBean.mappingColumnBeanList
        if (!mappingColumnBeanList.isEmpty()) {
            for (mappingColumnBean in mappingColumnBeanList) {
                if (mappingColumnBean is AnnotationMappingColumnBean) {
                    createTableSql.append(mappingColumnBean.column)
                    createTableSql.append(" " + mappingColumnBean.condition + Constants.Symbol.COMMA)
                    if (mappingColumnBean.isId) {
                        createTableSql.append("PRIMARY KEY (" + mappingColumnBean.column + ")")
                        createTableSql.append(Constants.Symbol.COMMA)
                    }
                }
            }
            createTableSql.delete(createTableSql.length - 1, createTableSql.length)
        }
        createTableSql.append(") " + annotationMappingBean.condition!!)
        createTableSql.append(Constants.Symbol.SEMICOLON)
        sqlList.add(createTableSql.toString())
        return sqlList.toTypedArray()
    }

    class SqlUtilException(cause: Throwable) : RuntimeException(cause)

    interface SqlProcessor {

        /**
         * statement process,for statement use
         * @param preparedStatement
         * @param parameter
         */
        fun statementProcess(preparedStatement: PreparedStatement, index: Int, parameter: Any?)

        /**
         * after select process,for result set to object
         * @param parameterType
         * @param resultSet
         * @param columnName
         * @return Object
         */
        fun afterSelectProcess(parameterType: KClass<*>, resultSet: ResultSet, columnName: String): Any?

        /**
         * before insert process,for generate insert sql
         * @param <T>
         * @param clazz
         * @param value
         * @return String
        </T> */
        fun <T : Any> beforeInsertProcess(clazz: KClass<T>, value: Any?): String

        /**
         * before update process,for generate update sql
         * @param <T>
         * @param clazz
         * @param isId
         * @param columnName
         * @param value
         * @return String
        </T> */
        fun <T : Any> beforeUpdateProcess(clazz: KClass<T>, isId: Boolean, columnName: String, value: Any?): String

        /**
         * before delete process,for generate delete sql
         * @param <T>
         * @param clazz
         * @param isId
         * @param columnName
         * @param value
         * @return String
        </T> */
        fun <T : Any> beforeDeleteProcess(clazz: KClass<T>, isId: Boolean, columnName: String, value: Any?): String
    }

    private enum class DeleteType {
        ONE_ROW, MULTIPLE_ROW
    }
}