package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.MappingNotFoundException
import com.oneliang.ktx.frame.ConfigurationFactory
import com.oneliang.ktx.frame.bean.Page
import java.sql.Connection
import kotlin.reflect.KClass

class OracleQueryImpl : DefaultQueryImpl() {

    /**
     *
     * Method: select object pagination list,has implement,it is sql binding
     * @param <T>
     * @param clazz
     * @param page
     * @param selectColumns
     * @param table
     * @param condition
     * @param parameters
     * @return List<T>
     * @throws QueryException
    </T></T> */
    @Throws(QueryException::class)
    override fun <T : Any> selectObjectPaginationList(clazz: KClass<T>, page: Page, selectColumns: Array<String>, table: String, condition: String, parameters: Array<Any>): List<T> {
        var tempSelectColumns = selectColumns
        var tempTable = table
        val totalRows = this.totalRows(clazz, tempTable, condition, parameters)
        val rowsPerPage = page.rowsPerPage
        page.initialize(totalRows, rowsPerPage)
        val startRow = page.pageFirstRow
        val currentPage = page.page
        //generate table string
        val rowNumAlias = "rn"
        val tableAlias = "t"
        if (tempSelectColumns.isEmpty()) {
            tempSelectColumns = arrayOf(tableAlias + Constants.Symbol.DOT + Constants.Symbol.WILDCARD)
        }
        val newColumns = Array(tempSelectColumns.size + 1) { Constants.String.BLANK }
        System.arraycopy(tempSelectColumns, 0, newColumns, 0, tempSelectColumns.size)
        newColumns[tempSelectColumns.size] = "rownum $rowNumAlias"
        if (tempTable.isBlank()) {
            val mappingBean = ConfigurationFactory.singletonConfigurationContext.findMappingBean(clazz) ?: throw MappingNotFoundException("can not find the mapping bean: $clazz")
            tempTable = mappingBean.table
        }
        tempTable = "$tempTable $tableAlias"
        tempTable = SqlUtil.selectSql(newColumns, tempTable, condition)
        tempTable = Constants.Symbol.BRACKET_LEFT + tempTable + Constants.Symbol.BRACKET_RIGHT
        //generate outer conditions
        val sqlConditions = StringBuilder()
        sqlConditions.append("and " + rowNumAlias + ">" + startRow + " and " + rowNumAlias + "<=" + rowsPerPage * currentPage)
        val list: List<T>
        var connection: Connection? = null
        try {
            connection = this.connectionPool!!.resource!!
            list = this.executeQuery(connection, clazz, emptyArray(), tempTable, sqlConditions.toString(), parameters)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool!!.releaseResource(connection)
        }
        return list
    }
}
