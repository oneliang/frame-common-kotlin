package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.MappingNotFoundException
import com.oneliang.ktx.frame.ConfigurationFactory
import com.oneliang.ktx.util.common.ObjectUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.sql.*
import kotlin.reflect.KClass

open class BaseQueryImpl : BaseQuery {
    companion object {
        private val logger = LoggerManager.getLogger(BaseQueryImpl::class)
        private val DEFAULT_SQL_PROCESSOR = DefaultSqlProcessor()
    }

    private var sqlProcessor: SqlUtil.SqlProcessor = DEFAULT_SQL_PROCESSOR

    /**
     *
     * Method: execute by sql,for all sql
     * @param connection
     * @param sql
     * @param parameters
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBySql(connection: Connection, sql: String, parameters: Array<Any>) {
        var parsedSql = sql
        var preparedStatement: PreparedStatement? = null
        try {
            parsedSql = DatabaseMappingUtil.parseSql(parsedSql)
            logger.info(parsedSql)
            preparedStatement = connection.prepareStatement(parsedSql)
            var index = 1
            for (parameter in parameters) {
                this.sqlProcessor.statementProcess(preparedStatement, index, parameter)
                index++
            }
            preparedStatement.execute()
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close()
                } catch (e: Exception) {
                    throw QueryException(e)
                }
            }
        }
    }

    /**
     *
     * Through the class generate the sql
     *
     * Method: execute query base on connection and  class and selectColumns and table and condition
     * @param <T>
     * @param connection
     * @param clazz
     * @param selectColumns
     * @param table
     * @param condition
     * @param parameters
     * @return list<T>
     * @throws QueryException
    </T></T> */
    @Throws(QueryException::class)
    override fun <T : Any> executeQuery(connection: Connection, clazz: KClass<T>, selectColumns: Array<String>, table: String, condition: String, parameters: Array<Any>): List<T> {
        var resultSet: ResultSet? = null
        val list: List<T>
        try {
            val mappingBean = ConfigurationFactory.singletonConfigurationContext.findMappingBean(clazz) ?: throw MappingNotFoundException("Mapping is not found, class:$clazz")
            val sql = SqlUtil.selectSql<Any>(selectColumns, table, condition, mappingBean)
            resultSet = this.executeQueryBySql(connection, sql, parameters)
            list = SqlUtil.resultSetToObjectList(resultSet, clazz, mappingBean, this.sqlProcessor)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.statement.close()
                    resultSet.close()
                } catch (e: Exception) {
                    throw QueryException(e)
                }

            }
        }
        return list
    }

    /**
     *
     * Method: execute query by id
     * @param <T>
     * @param connection
     * @param clazz
     * @param id
     * @return T
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any, IdType : Any> executeQueryById(connection: Connection, clazz: KClass<T>, id: IdType): T? {
        var instance: T? = null
        val list: List<T>
        var resultSet: ResultSet? = null
        try {
            val mappingBean = ConfigurationFactory.singletonConfigurationContext.findMappingBean(clazz) ?: throw MappingNotFoundException("Mapping is not found, class:$clazz")
            val sql = SqlUtil.classToSelectIdSql(clazz, mappingBean)
            resultSet = this.executeQueryBySql(connection, sql, arrayOf<Any>(id))
            list = SqlUtil.resultSetToObjectList(resultSet, clazz, mappingBean, this.sqlProcessor)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.statement.close()
                    resultSet.close()
                } catch (e: Exception) {
                    throw QueryException(e)
                }

            }
        }
        if (list.isNotEmpty()) {
            instance = list[0]
        }
        return instance
    }

    /**
     *
     * The base sql query
     *
     * Method: execute query base on the connection and sql command
     * @param connection
     * @param clazz
     * @param sql
     * @param parameters
     * @return List
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun <T : Any> executeQueryBySql(connection: Connection, clazz: KClass<T>, sql: String, parameters: Array<Any>): List<T> {
        var resultSet: ResultSet? = null
        val list: List<T>
        try {
            val mappingBean = ConfigurationFactory.singletonConfigurationContext.findMappingBean(clazz) ?: throw MappingNotFoundException("Mapping is not found, class:$clazz")
            resultSet = this.executeQueryBySql(connection, sql, parameters)
            list = SqlUtil.resultSetToObjectList(resultSet, clazz, mappingBean, this.sqlProcessor)
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.statement.close()
                    resultSet.close()
                } catch (e: Exception) {
                    throw QueryException(e)
                }

            }
        }
        return list
    }

    /**
     *
     * Method: execute query base on the connection and sql command
     *
     * Caution: use this method must get Statement from the ResultSet and close it and close the ResultSet
     * @param connection
     * @param sql
     * @param parameters
     * @return ResultSet
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeQueryBySql(connection: Connection, sql: String, parameters: Array<Any>): ResultSet {
        val resultSet: ResultSet
        try {
            val parsedSql = DatabaseMappingUtil.parseSql(sql)
            logger.info(parsedSql)
            val preparedStatement = connection.prepareStatement(parsedSql)
            if (parameters.isNotEmpty()) {
                var index = 1
                for (parameter in parameters) {
                    this.sqlProcessor.statementProcess(preparedStatement, index, parameter)
                    index++
                }
            }
            resultSet = preparedStatement.executeQuery()
        } catch (e: Exception) {
            throw QueryException(e)
        }
        return resultSet
    }

    /**
     *
     * Method: execute insert
     * @param connection
     * @param instance
     * @param table
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun <T : Any> executeInsert(connection: Connection, instance: T, table: String): Int {
        return this.executeUpdate(connection, instance, table, Constants.String.BLANK, BaseQuery.ExecuteType.INSERT)
    }

    /**
     *
     * Method: execute insert for auto increment and return the auto increment id
     * @param connection
     * @param <T>
     * @param table
     * @return int for id
     * @throws QueryException
    </T> */
    override fun <T : Any> executeInsertForAutoIncrement(connection: Connection, instance: T, table: String): Int {
        val id: Int
        try {
            val clazz = instance::class
            val mappingBean = ConfigurationFactory.singletonConfigurationContext.findMappingBean(clazz) ?: throw MappingNotFoundException("Mapping is not found, class:$clazz")
            val parameters = mutableListOf<Any>()
            val sql = SqlInjectUtil.objectToInsertSql(instance, table, mappingBean, parameters)
            id = this.executeInsertForAutoIncrementBySql(connection, sql, parameters.toTypedArray())
        } catch (e: Exception) {
            throw QueryException(e)
        }
        return id
    }

    /**
     *
     * Method: execute insert collection(list),transaction
     * @param <T>
     * @param connection
     * @param collection
     * @param table
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> executeInsert(connection: Connection, collection: Collection<T>, table: String): IntArray {
        return this.executeUpdate(connection, collection, table, BaseQuery.ExecuteType.INSERT)
    }

    /**
     *
     * Method: execute update
     * @param connection
     * @param instance
     * @param table
     * @param condition
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun <T : Any> executeUpdate(connection: Connection, instance: T, table: String, condition: String): Int {
        return this.executeUpdate(connection, instance, table, condition, BaseQuery.ExecuteType.UPDATE_BY_ID)
    }

    /**
     *
     * Method: execute update collection,transaction
     * @param <T>
     * @param connection
     * @param collection
     * @param table
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> executeUpdate(connection: Connection, collection: Collection<T>, table: String): IntArray {
        return this.executeUpdate(connection, collection, table, BaseQuery.ExecuteType.UPDATE_BY_ID)
    }

    /**
     *
     * Method: execute delete with id
     * @param <T>
     * @param connection
     * @param clazz
     * @param id
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any, IdType : Any> executeDeleteById(connection: Connection, clazz: KClass<T>, id: IdType): Int {
        val sql: String
        try {
            val mappingBean = ConfigurationFactory.singletonConfigurationContext.findMappingBean(clazz) ?: throw MappingNotFoundException("Mapping is not found, class:$clazz")
            sql = SqlUtil.classToDeleteOneRowSql(clazz, id, mappingBean)
        } catch (e: Exception) {
            throw QueryException(e)
        }
        return executeUpdateBySql(connection, sql)
    }

    /**
     *
     * Method: execute delete with multi id,transaction
     * @param <T>
     * @param connection
     * @param clazz
     * @param ids
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any, IdType : Any> executeDeleteByIds(connection: Connection, clazz: KClass<T>, ids: Array<IdType>): Int {
        val updateResult: Int
        try {
            val mappingBean = ConfigurationFactory.singletonConfigurationContext.findMappingBean(clazz) ?: throw MappingNotFoundException("Mapping is not found, class:$clazz")
            val sql = SqlUtil.classToDeleteMultipleRowSql(clazz, ids, mappingBean)
            updateResult = this.executeUpdateBySql(connection, sql)
        } catch (e: Exception) {
            throw QueryException(e)
        }
        return updateResult
    }

    /**
     *
     * Method: execute delete,condition of auto generate include by id
     * @param connection
     * @param instance
     * @param table
     * @param condition
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun <T : Any> executeDelete(connection: Connection, instance: T, table: String, condition: String): Int {
        return this.executeUpdate(connection, instance, table, condition, BaseQuery.ExecuteType.DELETE_BY_ID)
    }

    /**
     *
     * Method: execute delete
     * @param <T>
     * @param connection
     * @param clazz
     * @param condition
     * @param parameters
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> executeDelete(connection: Connection, clazz: KClass<T>, condition: String, parameters: Array<Any>): Int {
        val result: Int
        try {
            val mappingBean = ConfigurationFactory.singletonConfigurationContext.findMappingBean(clazz) ?: throw MappingNotFoundException("Mapping is not found, class:$clazz")
            val sql = SqlUtil.deleteSql(Constants.String.BLANK, condition, mappingBean)
            result = this.executeUpdateBySql(connection, sql, parameters)
        } catch (e: Exception) {
            throw QueryException(e)
        }

        return result
    }

    /**
     *
     * Method: execute delete collection,transaction
     * @param <T>
     * @param connection
     * @param collection
     * @param table
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    override fun <T : Any> executeDelete(connection: Connection, collection: Collection<T>, table: String): IntArray {
        return this.executeUpdate(connection, collection, table, BaseQuery.ExecuteType.DELETE_BY_ID)
    }

    /**
     * Method: execute insert for auto increment by sql and return the auto increment id
     * @param connection
     * @param sql
     * @param parameters
     * @return int id
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeInsertForAutoIncrementBySql(connection: Connection, sql: String, parameters: Array<Any>): Int {
        var preparedStatement: PreparedStatement? = null
        var id: Int = 0
        var resultSet: ResultSet? = null
        try {
            val parsedSql = DatabaseMappingUtil.parseSql(sql)
            logger.info(parsedSql)
            preparedStatement = connection.prepareStatement(parsedSql, Statement.RETURN_GENERATED_KEYS)
            var index = 1
            for (parameter in parameters) {
                this.sqlProcessor.statementProcess(preparedStatement, index, parameter)
                index++
            }
            preparedStatement!!.execute()
            resultSet = preparedStatement.generatedKeys
            if (resultSet != null && resultSet.next()) {
                id = resultSet.getInt(1)
            }
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close()
                } catch (e: Exception) {
                    throw QueryException(e)
                }
            }
            if (resultSet != null) {
                try {
                    resultSet.close()
                } catch (e: Exception) {
                    throw QueryException(e)
                }
            }
        }
        return id
    }

    /**
     *
     * Method: execute update include insert sql and update sql,for sql binding
     * @param connection
     * @param instance
     * @param table
     * @param executeType
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    protected fun <T : Any> executeUpdate(connection: Connection, instance: T, table: String, condition: String, executeType: BaseQuery.ExecuteType): Int {
        val rows: Int
        try {
            val clazz = instance::class
            val mappingBean = ConfigurationFactory.singletonConfigurationContext.findMappingBean(clazz) ?: throw MappingNotFoundException("Mapping is not found, class:$clazz")
            val parameters = mutableListOf<Any>()
            val sql = when (executeType) {
                BaseQuery.ExecuteType.INSERT -> SqlInjectUtil.objectToInsertSql(instance, table, mappingBean, parameters)
                BaseQuery.ExecuteType.UPDATE_BY_ID -> SqlInjectUtil.objectToUpdateSql(instance, table, condition, true, mappingBean, parameters)
                BaseQuery.ExecuteType.UPDATE_NOT_BY_ID -> SqlInjectUtil.objectToUpdateSql(instance, table, condition, false, mappingBean, parameters)
                BaseQuery.ExecuteType.DELETE_BY_ID -> SqlInjectUtil.objectToDeleteSql(instance, table, condition, true, mappingBean, parameters)
                BaseQuery.ExecuteType.DELETE_NOT_BY_ID -> SqlInjectUtil.objectToDeleteSql(instance, table, condition, false, mappingBean, parameters)
            }
            rows = this.executeUpdateBySql(connection, sql, parameters.toTypedArray())
        } catch (e: Exception) {
            throw QueryException(e)
        }

        return rows
    }

    /**
     *
     * Method: execute update collection,transaction not for sql binding
     * @param <T>
     * @param connection
     * @param collection
     * @param table
     * @param executeType
     * @return int[]
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    protected fun <T : Any> executeUpdate(connection: Connection, collection: Collection<T>, table: String, executeType: BaseQuery.ExecuteType): IntArray {
        var rows = IntArray(0)
        if (collection.isNotEmpty()) {
            try {
                val sqls = Array(collection.size) { Constants.String.BLANK }
                for ((i, instance) in collection.withIndex()) {
                    val clazz = instance::class
                    val mappingBean = ConfigurationFactory.singletonConfigurationContext.findMappingBean(clazz) ?: throw MappingNotFoundException("Mapping is not found, class:$clazz")
                    when (executeType) {
                        BaseQuery.ExecuteType.INSERT -> sqls[i] = SqlUtil.objectToInsertSql(instance, table, mappingBean, this.sqlProcessor)
                        BaseQuery.ExecuteType.UPDATE_BY_ID -> sqls[i] = SqlUtil.objectToUpdateSql(instance, table, Constants.String.BLANK, true, mappingBean, this.sqlProcessor)
                        BaseQuery.ExecuteType.UPDATE_NOT_BY_ID -> sqls[i] = SqlUtil.objectToUpdateSql(instance, table, Constants.String.BLANK, false, mappingBean, this.sqlProcessor)
                        BaseQuery.ExecuteType.DELETE_BY_ID -> sqls[i] = SqlUtil.objectToDeleteSql(instance, table, Constants.String.BLANK, true, mappingBean, this.sqlProcessor)
                        BaseQuery.ExecuteType.DELETE_NOT_BY_ID -> sqls[i] = SqlUtil.objectToDeleteSql(instance, table, Constants.String.BLANK, false, mappingBean, this.sqlProcessor)
                    }
                }
                rows = this.executeBatch(connection, sqls)
            } catch (e: Exception) {
                throw QueryException(e)
            }
        }
        return rows
    }

    /**
     *
     * Method: execute update collection,transaction,for preparedStatement sql binding
     * @param <T>
     * @param <M>
     * @param connection
     * @param collection
     * @param clazz mapping class
     * @param table
     * @return int[]
     * @throws QueryException
    </M></T> */
    @Throws(QueryException::class)
    protected fun <T : Any, M : Any> executeUpdate(connection: Connection, collection: Collection<T>, clazz: KClass<M>, table: String, executeType: BaseQuery.ExecuteType): IntArray {
        var rows = IntArray(0)
        var preparedStatement: PreparedStatement? = null
        if (collection.isEmpty()) {
            logger.warning("collection is empty, class:$clazz")
            return rows
        }
        var customTransaction = false
        val customTransactionSign = TransactionManager.customTransactionSign.get()
        if (customTransactionSign != null && customTransactionSign) {
            customTransaction = true
        }
        try {
            val mappingBean = ConfigurationFactory.singletonConfigurationContext.findMappingBean(clazz) ?: throw MappingNotFoundException("Mapping is not found, class:$clazz")
            val fieldNameList = mutableListOf<String>()
            var sql = when (executeType) {
                BaseQuery.ExecuteType.INSERT -> SqlInjectUtil.classToInsertSql(clazz, table, mappingBean, fieldNameList)
                BaseQuery.ExecuteType.UPDATE_BY_ID -> SqlInjectUtil.classToUpdateSql(clazz, table, Constants.String.BLANK, true, mappingBean, fieldNameList)
                BaseQuery.ExecuteType.UPDATE_NOT_BY_ID -> SqlInjectUtil.classToUpdateSql(clazz, table, Constants.String.BLANK, false, mappingBean, fieldNameList)
                BaseQuery.ExecuteType.DELETE_BY_ID -> SqlInjectUtil.classToDeleteSql(clazz, table, Constants.String.BLANK, true, mappingBean, fieldNameList)
                BaseQuery.ExecuteType.DELETE_NOT_BY_ID -> SqlInjectUtil.classToDeleteSql(clazz, table, Constants.String.BLANK, false, mappingBean, fieldNameList)
            }
            sql = DatabaseMappingUtil.parseSql(sql)
            logger.info(sql)
            if (!customTransaction) {
                connection.autoCommit = false
            }
            preparedStatement = connection.prepareStatement(sql)
            for (instance in collection) {
                var index = 1
                for (fieldName in fieldNameList) {
                    val parameter = ObjectUtil.getterOrIsMethodInvoke(instance, fieldName)
                    this.sqlProcessor.statementProcess(preparedStatement, index, parameter)
                    index++
                }
                preparedStatement.addBatch()
            }
            rows = preparedStatement.executeBatch()
            preparedStatement.clearBatch()
            if (!customTransaction) {
                connection.commit()
            }
        } catch (e: Exception) {
            if (!customTransaction) {
                try {
                    connection.rollback()
                } catch (ex: Exception) {
                    throw QueryException(ex)
                }

            }
            throw QueryException(e)
        } finally {
            try {
                if (!customTransaction) {
                    connection.autoCommit = true
                }
                preparedStatement?.close()
            } catch (e: Exception) {
                throw QueryException(e)
            }
        }
        return rows
    }

    /**
     *
     * Method: execute update by sql statement
     * @param connection
     * @param sql include insert delete update
     * @param parameters
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeUpdateBySql(connection: Connection, sql: String, parameters: Array<Any>): Int {
        var preparedStatement: PreparedStatement? = null
        val updateResult: Int
        try {
            val parsedSql = DatabaseMappingUtil.parseSql(sql)
            logger.info(parsedSql)
            preparedStatement = connection.prepareStatement(parsedSql)
            var index = 1
            for (parameter in parameters) {
                this.sqlProcessor.statementProcess(preparedStatement, index, parameter)
                index++
            }
            updateResult = preparedStatement!!.executeUpdate()
        } catch (e: Exception) {
            throw QueryException(e)
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close()
                } catch (e: Exception) {
                    throw QueryException(e)
                }
            }
        }
        return updateResult
    }

    /**
     *
     * Method: execute batch by connection,transaction
     * @param connection
     * @param sqls
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBatch(connection: Connection, sqls: Array<String>): IntArray {
        var statement: Statement? = null
        val results: IntArray
        var customTransaction = false
        val customTransactionSign = TransactionManager.customTransactionSign.get()
        if (customTransactionSign != null && customTransactionSign) {
            customTransaction = true
        }
        try {
            if (!customTransaction) {
                connection.autoCommit = false
            }
            statement = connection.createStatement()
            for (sql in sqls) {
                val parsedSql = DatabaseMappingUtil.parseSql(sql)
                logger.info(parsedSql)
                statement.addBatch(parsedSql)
            }
            results = statement.executeBatch()
            if (!customTransaction) {
                connection.commit()
            }
        } catch (e: Exception) {
            if (!customTransaction) {
                try {
                    connection.rollback()
                } catch (ex: SQLException) {
                    throw QueryException(ex)
                }
            }
            throw QueryException(e)
        } finally {
            try {
                if (!customTransaction) {
                    connection.autoCommit = true
                }
                statement?.close()
            } catch (e: Exception) {
                throw QueryException(e)
            }
        }
        return results
    }

    /**
     *
     * Method: execute batch by connection,transaction
     * @param connection
     * @param sql include insert update delete sql only the same sql many data
     * @param parametersList
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBatch(connection: Connection, sql: String, parametersList: List<Array<Any>>): IntArray {
        var results = IntArray(0)
        var preparedStatement: PreparedStatement? = null
        if (parametersList.isEmpty()) {
            return results
        }
        var customTransaction = false
        val customTransactionSign = TransactionManager.customTransactionSign.get()
        if (customTransactionSign != null && customTransactionSign) {
            customTransaction = true
        }
        try {
            val parsedSql = DatabaseMappingUtil.parseSql(sql)
            logger.info(parsedSql)
            if (!customTransaction) {
                connection.autoCommit = false
            }
            preparedStatement = connection.prepareStatement(parsedSql)
            for (parameters in parametersList) {
                var index = 1
                for (parameter in parameters) {
                    this.sqlProcessor.statementProcess(preparedStatement, index, parameter)
                    index++
                }
                preparedStatement.addBatch()
            }
            results = preparedStatement.executeBatch()
            preparedStatement.clearBatch()
            if (!customTransaction) {
                connection.commit()
            }
        } catch (e: Exception) {
            if (!customTransaction) {
                try {
                    connection.rollback()
                } catch (ex: Exception) {
                    throw QueryException(ex)
                }
            }
            throw QueryException(e)
        } finally {
            try {
                if (!customTransaction) {
                    connection.autoCommit = true
                }
                preparedStatement?.close()
            } catch (e: Exception) {
                throw QueryException(e)
            }
        }
        return results
    }

    /**
     *
     * Method: execute batch by connection,transaction
     * @param connection
     * @param batchObjectCollection
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    override fun executeBatch(connection: Connection, batchObjectCollection: Collection<BaseQuery.BatchObject>): IntArray {
        var results = IntArray(0)
        if (batchObjectCollection.isEmpty()) {
            return results
        }
        try {
            val sqls = Array(batchObjectCollection.size) { Constants.String.BLANK }
            for ((i, batchObject) in batchObjectCollection.withIndex()) {
                val instance = batchObject.instance
                val executeType = batchObject.excuteType
                val condition = batchObject.condition
                val clazz = instance.javaClass
                val mappingBean = ConfigurationFactory.singletonConfigurationContext.findMappingBean(clazz.kotlin) ?: throw MappingNotFoundException("Mapping is not found, class:$clazz")
                when (executeType) {
                    BaseQuery.ExecuteType.INSERT -> sqls[i] = SqlUtil.objectToInsertSql(instance, Constants.String.BLANK, mappingBean, this.sqlProcessor)
                    BaseQuery.ExecuteType.UPDATE_BY_ID -> sqls[i] = SqlUtil.objectToUpdateSql(instance, Constants.String.BLANK, condition, true, mappingBean, this.sqlProcessor)
                    BaseQuery.ExecuteType.UPDATE_NOT_BY_ID -> sqls[i] = SqlUtil.objectToUpdateSql(instance, Constants.String.BLANK, condition, false, mappingBean, this.sqlProcessor)
                    BaseQuery.ExecuteType.DELETE_BY_ID -> sqls[i] = SqlUtil.objectToDeleteSql(instance, Constants.String.BLANK, condition, true, mappingBean, this.sqlProcessor)
                    BaseQuery.ExecuteType.DELETE_NOT_BY_ID -> sqls[i] = SqlUtil.objectToDeleteSql(instance, Constants.String.BLANK, condition, false, mappingBean, this.sqlProcessor)
                }
            }
            results = this.executeBatch(connection, sqls)
        } catch (e: Exception) {
            throw QueryException(e)
        }
        return results
    }

    /**
     * @param sqlProcessor the sqlProcessor to set
     */
    fun setSqlProcessor(sqlProcessor: SqlUtil.SqlProcessor) {
        this.sqlProcessor = sqlProcessor
    }
}
