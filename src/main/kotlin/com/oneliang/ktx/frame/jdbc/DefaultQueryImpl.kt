package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ConfigurationFactory
import com.oneliang.ktx.frame.bean.Page
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.resource.ResourcePool
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass

/**
 * all QueryImpl can extends DefaultQueryImpl,but must initialize the property
 * @author lwx
 * @since 2011-02-12
 */
open class DefaultQueryImpl : BaseQueryImpl(), Query {
    companion object {
        private val logger = LoggerManager.getLogger(DefaultQueryImpl::class)
    }

    override lateinit var connectionPool: ResourcePool<Connection>

    /**
     *
     * Method: delete object,by table condition just by object id,sql binding
     * @param <T>
     * @param instance
     * @param table
     * @param condition
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> deleteObject(instance: T, table: String, condition: String): Int {
        return this.executeUpdate(instance, table, condition, BaseQuery.ExecuteType.DELETE_BY_ID)
    }

    /**
     *
     * Method: delete object not by id,by table condition,sql binding
     * @param <T>
     * @param instance
     * @param table
     * @param condition
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> deleteObjectNotById(instance: T, table: String, condition: String): Int {
        return this.executeUpdate(instance, table, condition, BaseQuery.ExecuteType.DELETE_NOT_BY_ID)
    }

    /**
     *
     * Method: delete class,by condition
     * @param <T>
     * @param kClass
     * @param condition
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> deleteObject(kClass: KClass<T>, condition: String, parameters: Array<*>): Int {
        val result: Int
        var connection: Connection? = null
        try {
            connection = this.connectionPool.resource!!
            result = this.executeDelete(connection, kClass, condition, parameters)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseResource(connection)
        }
        return result
    }

    /**
     *
     * Method: delete object collection,transaction,not sql binding
     * @param <T>
     * @param collection
     * @param table
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> deleteObject(collection: Collection<T>, table: String): IntArray {
        return this.executeUpdate(collection, table, BaseQuery.ExecuteType.DELETE_BY_ID)
    }

    /**
     *
     * Method: delete object collection,transaction,for sql binding
     * @param <T>
     * @param <M>
     * @param collection
     * @param kClass
     * @param table
     * @return int[]
     * @throws QueryException
    </M></T> */
    @Throws(QueryException::class)
    override fun <T : Any, M : Any> deleteObject(collection: Collection<T>, kClass: KClass<M>, table: String): IntArray {
        return this.executeUpdate(collection, kClass, table, BaseQuery.ExecuteType.DELETE_BY_ID)
    }

    /**
     *
     * Method: delete object by id,not sql binding
     * @param <T>
     * @param kClass
     * @param id
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any, IdType : Any> deleteObjectById(kClass: KClass<T>, id: IdType): Int {
        val updateResult: Int
        var connection: Connection? = null
        try {
            connection = this.connectionPool.resource!!
            updateResult = this.executeDeleteById(connection, kClass, id)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseResource(connection)
        }
        return updateResult
    }

    /**
     *
     * Method: delete object by multiple id,transaction,not sql binding
     * @param <T>
     * @param kClass
     * @param ids
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any, IdType : Any> deleteObjectByIds(kClass: KClass<T>, ids: Array<IdType>): Int {
        if (ids.isEmpty()) {
            return 0
        }
        val updateResult: Int
        var connection: Connection? = null
        try {
            connection = this.connectionPool.resource!!
            updateResult = this.executeDeleteByIds(connection, kClass, ids)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseResource(connection)
        }
        return updateResult
    }

    /**
     *
     * Method: insert object for sql binding
     * @param <T>
     * @param instance
     * @param table
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> insertObject(instance: T, table: String): Int {
        return this.executeUpdate(instance, table, Constants.String.BLANK, BaseQuery.ExecuteType.INSERT)
    }

    /**
     *
     * Method: insert object for sql binding and return the auto increment id
     * @param <T>
     * @param instance
     * @param table
     * @return int
     * @throws QueryException
    </T> */
    override fun <T : Any> insertObjectForAutoIncrement(instance: T, table: String): Int {
        return this.executeInsertForAutoIncrement(instance, table)
    }

    /**
     *
     * Method: insert object collection,transaction,not for sql binding
     * @param <T>
     * @param collection
     * @param table
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> insertObject(collection: Collection<T>, table: String): IntArray {
        return this.executeUpdate(collection, table, BaseQuery.ExecuteType.INSERT)
    }

    /**
     *
     * Method: insert object collection,transaction,for sql binding
     * @param <T>
     * @param <M>
     * @param collection
     * @param kClass mapping class
     * @param table
     * @return int[]
     * @throws QueryException
    </M></T> */
    @Throws(QueryException::class)
    override fun <T : Any, M : Any> insertObject(collection: Collection<T>, kClass: KClass<M>, table: String): IntArray {
        return this.executeUpdate(collection, kClass, table, BaseQuery.ExecuteType.INSERT)
    }

    /**
     *
     * Method: update object,by table,condition,sql binding,null value field is not update
     * @param <T>
     * @param instance
     * @param table
     * @param condition
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> updateObject(instance: T, table: String, condition: String): Int {
        return this.executeUpdate(instance, table, condition, BaseQuery.ExecuteType.UPDATE_BY_ID)
    }

    /**
     *
     * Method: update object not by id,by table,condition,sql binding,null value field is not update
     * @param <T>
     * @param instance
     * @param table
     * @param condition
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> updateObjectNotById(instance: T, table: String, condition: String): Int {
        return this.executeUpdate(instance, table, condition, BaseQuery.ExecuteType.UPDATE_NOT_BY_ID)
    }

    /**
     *
     * Method: update object collection,transaction,not for sql binding
     * @param <T>
     * @param collection
     * @param table
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> updateObject(collection: Collection<T>, table: String): IntArray {
        return this.executeUpdate(collection, table, BaseQuery.ExecuteType.UPDATE_BY_ID)
    }

    /**
     *
     * Method: update object collection,transaction,for sql binding
     * @param <T>
     * @param <M>
     * @param collection
     * @param kClass mapping class
     * @param table
     * @return int[]
     * @throws QueryException
    </M></T> */
    @Throws(QueryException::class)
    override fun <T : Any, M : Any> updateObject(collection: Collection<T>, kClass: KClass<M>, table: String): IntArray {
        return this.executeUpdate(collection, kClass, table, BaseQuery.ExecuteType.UPDATE_BY_ID)
    }

    /**
     *
     * Method: select object by id
     * @param <T>
     * @param kClass
     * @param id
     * @return T
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any, IdType : Any> selectObjectById(kClass: KClass<T>, id: IdType): T? {
        val instance: T?
        var connection: Connection? = null
        try {
            connection = this.connectionPool.stableResource!!
            instance = this.executeQueryById(connection, kClass, id)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseStableResource(connection)
        }
        return instance
    }

    /**
     *
     * Method: select object list,by column,table,condition,parameters,it is sql binding
     * @param <T>
     * @param kClass
     * @param selectColumns
     * @param table
     * @param condition
     * @param parameters
     * @return List<T>
     * @throws QueryException
    </T></T> */
    @Throws(QueryException::class)
    override fun <T : Any> selectObjectList(kClass: KClass<T>, selectColumns: Array<String>, table: String, condition: String, parameters: Array<*>): List<T> {
        val list: List<T>
        var connection: Connection? = null
        try {
            connection = this.connectionPool.stableResource!!
            list = this.executeQuery(connection, kClass, selectColumns, table, condition, parameters)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseStableResource(connection)
        }
        return list
    }

    /**
     *
     * Method: select object list by sql,it is sql binding
     * @param <T>
     * @param kClass
     * @param sql
     * @param parameters
     * @return List<T>
     * @throws QueryException
    </T></T> */
    @Throws(QueryException::class)
    override fun <T : Any> selectObjectListBySql(kClass: KClass<T>, sql: String, parameters: Array<*>): List<T> {
        val list: List<T>
        var connection: Connection? = null
        try {
            connection = this.connectionPool.stableResource!!
            list = this.executeQueryBySql(connection, kClass, sql, parameters)
            logger.debug("sql select result:%s, sql%s", list.size, sql)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseStableResource(connection)
        }
        return list
    }

    /**
     *
     * Method: select object pagination list,has implement,it is sql binding
     * @param <T>
     * @param kClass
     * @param page
     * @param selectColumns
     * @param table
     * @param condition
     * @param parameters
     * @return List<T>
     * @throws QueryException
    </T></T> */
    @Throws(QueryException::class)
    override fun <T : Any> selectObjectPaginationList(kClass: KClass<T>, page: Page, selectColumns: Array<String>, table: String, condition: String, parameters: Array<*>): List<T> {
        val totalRows = this.totalRows(kClass, table, condition, parameters)
        val rowsPerPage = page.rowsPerPage
        page.initialize(totalRows, rowsPerPage)
        val startRow = page.pageFirstRow
        val sqlConditions = StringBuilder()
        sqlConditions.append(condition)
        sqlConditions.append(" " + Constants.Database.MySql.PAGINATION + " ")
        sqlConditions.append(startRow.toString() + Constants.Symbol.COMMA + rowsPerPage)
        return this.selectObjectList(kClass, selectColumns, table, sqlConditions.toString(), parameters)
    }

    /**
     *
     * Method: execute by sql ,for all sql,sql binding
     * @param sql
     * @param parameters
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBySql(sql: String, parameters: Array<*>) {
        var connection: Connection? = null
        try {
            connection = this.connectionPool.resource!!
            this.executeBySql(connection, sql, parameters)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseResource(connection)
        }
    }

    /**
     *
     * Method: execute query by sql statement,use caution,must close the statement
     * @param sql
     * @param parameters
     * @return ResultSet
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeQueryBySql(sql: String, parameters: Array<*>): ResultSet {
        val resultSet: ResultSet
        var connection: Connection? = null
        try {
            connection = this.connectionPool.resource!!
            resultSet = this.executeQueryBySql(connection, sql, parameters)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseResource(connection)
        }
        return resultSet
    }

    /**
     *
     * Method: execute insert for auto increment and return auto increment id
     * @param instance
     * @param table
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    protected fun <T : Any> executeInsertForAutoIncrement(instance: T, table: String): Int {
        val id: Int
        var connection: Connection? = null
        try {
            connection = this.connectionPool.resource!!
            id = this.executeInsertForAutoIncrement(connection, instance, table)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseResource(connection)
        }
        return id
    }

    /**
     *
     * Method: execute update
     * @param instance
     * @param table
     * @param executeType
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun <T : Any> executeUpdate(instance: T, table: String, condition: String, executeType: BaseQuery.ExecuteType): Int {
        val rows: Int
        var connection: Connection? = null
        try {
            connection = this.connectionPool.resource!!
            rows = this.executeUpdate(connection, instance, table, condition, executeType)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseResource(connection)
        }
        return rows
    }

    /**
     *
     * Method: execute update collection,transaction,not for sql binding
     * @param <T>
     * @param collection
     * @param table
     * @param executeType
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    protected fun <T : Any> executeUpdate(collection: Collection<T>, table: String, executeType: BaseQuery.ExecuteType): IntArray {
        val rows: IntArray
        var connection: Connection? = null
        try {
            connection = this.connectionPool.resource!!
            rows = this.executeUpdate(connection, collection, table, executeType)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseResource(connection)
        }
        return rows
    }

    /**
     *
     * Method: execute update collection,transaction,for sql binding
     * @param <T>
     * @param <M>
     * @param collection
     * @param kClass
     * @param table
     * @param executeType
     * @return int[]
     * @throws QueryException
    </M></T> */
    @Throws(QueryException::class)
    protected fun <T : Any, M : Any> executeUpdate(collection: Collection<T>, kClass: KClass<M>, table: String, executeType: BaseQuery.ExecuteType): IntArray {
        val rows: IntArray
        var connection: Connection? = null
        try {
            connection = this.connectionPool.resource!!
            rows = this.executeUpdate(connection, collection, kClass, table, executeType)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseResource(connection)
        }
        return rows
    }

    /**
     *
     * Method: execute update by sql statement it is sql binding
     * @param sql include insert delete update
     * @param parameters
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeUpdateBySql(sql: String, parameters: Array<*>): Int {
        var updateResult = 0
        var connection: Connection? = null
        try {
            connection = this.connectionPool.resource!!
            updateResult = this.executeUpdateBySql(connection, sql, parameters)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseResource(connection)
        }
        return updateResult
    }

    /**
     *
     * Method: execute batch,transaction
     * @param sqls
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBatch(sqls: Array<String>): IntArray {
        val results: IntArray
        var connection: Connection? = null
        try {
            connection = this.connectionPool.resource!!
            results = this.executeBatch(connection, sqls)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseResource(connection)
        }
        return results
    }

    /**
     *
     * Method: execute batch,transaction
     * @param sql include insert update delete sql only the same sql many data
     * @param parametersList
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBatch(sql: String, parametersList: List<Array<Any>>): IntArray {
        val results: IntArray
        var connection: Connection? = null
        try {
            connection = this.connectionPool.resource!!
            results = this.executeBatch(connection, sql, parametersList)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseResource(connection)
        }
        return results
    }

    /**
     *
     * Method: execute batch,transaction
     * @param batchObjectCollection
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBatch(batchObjectCollection: Collection<BaseQuery.BatchObject>): IntArray {
        val results: IntArray
        var connection: Connection? = null
        try {
            connection = this.connectionPool.resource!!
            results = this.executeBatch(connection, batchObjectCollection)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            this.connectionPool.releaseResource(connection)
        }
        return results
    }

    /**
     *
     * Method: get the total size,it is sql binding
     * @param <T>
     * @param table
     * @param condition
     * @param parameters
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> totalRows(table: String, condition: String, parameters: Array<*>): Int {
        return this.totalRows<Any>(null, table, condition, parameters)
    }

    /**
     *
     * Method: get the total size
     * @param <T>
     * @param kClass
     * @param table
     * @param condition
     * @param parameters
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> totalRows(kClass: KClass<T>?, table: String, condition: String, parameters: Array<*>): Int {
        val sql = if (kClass != null) {
            val mappingBean = ConfigurationFactory.singletonConfigurationContext.findMappingBean(kClass)
            SqlUtil.selectSql(arrayOf("COUNT(0) AS " + Constants.Database.COLUMN_NAME_TOTAL), table, condition, mappingBean)
        } else {
            SqlUtil.selectSql(arrayOf("COUNT(0) AS " + Constants.Database.COLUMN_NAME_TOTAL), table, condition, null)
        }
        val totalList = this.selectObjectListBySql(Total::class, sql, parameters)
        return if (totalList.isNotEmpty()) {
            totalList[0].total
        } else {
            0
        }
    }

    /**
     * execute transaction, if you need to stop transaction, you can throw exception
     * @param transaction
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeTransaction(transaction: Transaction): Boolean {
        return executeTransaction(transaction::execute)
    }

    @Throws(QueryException::class)
    override fun executeTransaction(transaction: () -> Boolean): Boolean {
        var isFirstIn = false
        val customTransactionSign = TransactionManager.customTransactionSign.get()
        if (customTransactionSign == null || !customTransactionSign) {
            isFirstIn = true
        }
        return if (isFirstIn) {
            TransactionManager.customTransactionSign.set(true)
            var connection: Connection? = null
            //beginTransaction
            try {
                connection = this.connectionPool.resource!!
                connection.autoCommit = false
                val result = transaction()
                if (!result) {
                    connection.rollback()
                } else {
                    connection.commit()
                }
                result
            } catch (e: Throwable) {
                try {
                    connection!!.rollback()
                } catch (e: Throwable) {
                    throw QueryException(e)
                }
                throw QueryException(e)
            } finally {
                //endTransaction
                try {
                    connection!!.autoCommit = true
                } catch (e: Throwable) {
                    throw QueryException(e)
                } finally {
                    TransactionManager.customTransactionSign.set(false)
                    this.connectionPool.releaseResource(connection)
                }
            }
        } else {//not first in
            val connection = this.connectionPool.resource!!
            try {
                val result = transaction()
                if (!result) {
                    connection.rollback()
                }
                result
            } catch (e: Throwable) {
                try {
                    connection.rollback()
                } catch (e: Throwable) {
                    throw QueryException(e)
                }
                throw QueryException(e)
            }
        }
    }
}
