package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.MappingNotFoundException
import com.oneliang.ktx.frame.ConfigurationFactory
import com.oneliang.ktx.util.common.parseStringGroup

/**
 * for db mapping file use,through the class can find the table column which
 * field map
 *
 * @author Dandelion
 * @since 2008-12-29
 */
object DatabaseMappingUtil {

    /**
     * regex
     */
    private val REGEX = "\\{[\\w\\.]*\\}"
    private val FIRST_REGEX = "\\{"

    /**
     * parse sql like:select * from {User}--can find the mapping file where {User.id} and so on
     * @param sql
     * @return the parse sql
     * @throws Exception
     */
    @Throws(MappingNotFoundException::class)
    fun parseSql(sql: String): String {
        var tempSql = sql
        val list = tempSql.parseStringGroup(REGEX, FIRST_REGEX, Constants.String.BLANK, 1)
        for (string in list) {
            val pos = string.lastIndexOf(Constants.Symbol.DOT)
            if (pos > 0) {
                val className = string.substring(0, pos)
                val fieldName = string.substring(pos + 1, string.length)
                val mappingBean = ConfigurationFactory.singletonConfigurationContext.findMappingBean(className)
                if (mappingBean != null) {
                    val column = mappingBean.getColumn(fieldName)
                    if (column.isNotBlank()) {
                        tempSql = tempSql.replaceFirst(REGEX.toRegex(), column)
                    } else {
                        throw MappingNotFoundException("can not find the mapping field: " + className + Constants.Symbol.DOT + fieldName)
                    }
                } else {
                    throw MappingNotFoundException("can not find the mapping bean: $className")
                }
            } else {
                val mappingBean = ConfigurationFactory.singletonConfigurationContext.findMappingBean(string)
                if (mappingBean != null) {
                    val table = mappingBean.table
                    if (table.isNotBlank()) {
                        tempSql = tempSql.replaceFirst(REGEX.toRegex(), table)
                    } else {
                        throw MappingNotFoundException("can not find the mapping table of the class:$string")
                    }
                } else {
                    throw MappingNotFoundException("can not find the mapping class:$string")
                }
            }
        }
        return tempSql
    }
}
