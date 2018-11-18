package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.bean.Page
import com.oneliang.ktx.util.resource.ResourcePool
import java.io.Serializable
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass

interface Query : BaseQuery {

    /**
     * @return the connectionPool
     */
    val connectionPool: ResourcePool<Connection>?

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
    fun <T : Any> deleteObject(instance: T, table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK): Int

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
    fun <T : Any> deleteObjectNotById(instance: T, table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK): Int

    /**
     *
     * Method: delete class,by condition
     * @param <T>
     * @param clazz
     * @param condition
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> deleteObject(clazz: KClass<T>, condition: String = Constants.String.BLANK): Int

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
    fun <T : Any> deleteObject(collection: Collection<T>, table: String = Constants.String.BLANK): IntArray

    /**
     *
     * Method: delete object collection,transaction,for sql binding
     * @param <T>
     * @param <M>
     * @param collection
     * @param clazz
     * @param table
     * @return int[]
     * @throws QueryException
    </M></T> */
    @Throws(QueryException::class)
    fun <T : Any, M : Any> deleteObject(collection: Collection<T>, clazz: KClass<M>, table: String = Constants.String.BLANK): IntArray

    /**
     *
     * Method: delete object by id,not sql binding
     * @param <T>
     * @param clazz
     * @param id
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> deleteObjectById(clazz: KClass<T>, id: Serializable): Int

    /**
     *
     * Method: delete object by multiple id,transaction,not sql binding
     * @param <T>
     * @param clazz
     * @param ids
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> deleteObjectByIds(clazz: KClass<T>, ids: Array<Serializable>): Int

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
    fun <T : Any> insertObject(instance: T, table: String = Constants.String.BLANK): Int

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
    fun <T : Any> insertObject(collection: Collection<T>, table: String = Constants.String.BLANK): IntArray

    /**
     *
     * Method: insert object collection,transaction,for sql binding
     * @param <T>
     * @param <M>
     * @param collection
     * @param clazz mapping class
     * @param table
     * @return int[]
     * @throws QueryException
    </M></T> */
    @Throws(QueryException::class)
    fun <T : Any, M : Any> insertObject(collection: Collection<T>, clazz: KClass<M>, table: String = Constants.String.BLANK): IntArray

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
    fun <T : Any> updateObject(instance: T, table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK): Int

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
    fun <T : Any> updateObjectNotById(instance: T, table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK): Int

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
    fun <T : Any> updateObject(collection: Collection<T>, table: String = Constants.String.BLANK): IntArray

    /**
     *
     * Method: update object collection,transaction,for sql binding
     * @param <T>
     * @param <M>
     * @param collection
     * @param clazz mapping class
     * @param table
     * @return int[]
     * @throws QueryException
    </M></T> */
    @Throws(QueryException::class)
    fun <T : Any, M : Any> updateObject(collection: Collection<T>, clazz: KClass<M>, table: String = Constants.String.BLANK): IntArray

    /**
     *
     * Method: select object by id
     * @param <T>
     * @param clazz
     * @param id
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> selectObjectById(clazz: KClass<T>, id: Serializable): T?

    /**
     *
     * Method: select object list,by column,table,condition,parameters,it is sql binding
     * @param <T>
     * @param clazz
     * @param selectColumns
     * @param table
     * @param condition
     * @param parameters
     * @return List<T>
     * @throws QueryException
    </T></T> */
    @Throws(QueryException::class)
    fun <T : Any> selectObjectList(clazz: KClass<T>, selectColumns: Array<String> = emptyArray(), table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK, parameters: Array<Any> = emptyArray()): List<T>

    /**
     *
     * Method: select object list by sql,it is sql binding
     * @param <T>
     * @param clazz
     * @param sql
     * @param parameters
     * @return List<T>
     * @throws QueryException
    </T></T> */
    @Throws(QueryException::class)
    fun <T : Any> selectObjectListBySql(clazz: KClass<T>, sql: String, parameters: Array<Any> = emptyArray()): List<T>

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
    fun <T : Any> selectObjectPaginationList(clazz: KClass<T>, page: Page, selectColumns: Array<String> = emptyArray(), table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK, parameters: Array<Any> = emptyArray()): List<T>

    /**
     *
     * Method: execute by sql ,for all sql,sql binding
     * @param sql
     * @param parameters
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeBySql(sql: String, parameters: Array<Any> = emptyArray())

    /**
     *
     * Method: execute query by sql statement,use caution,must close the statement
     * @param sql
     * @param parameters
     * @return ResultSet
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeQueryBySql(sql: String, parameters: Array<Any> = emptyArray()): ResultSet

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
    fun <T : Any> executeUpdate(instance: T, table: String, condition: String, executeType: BaseQuery.ExecuteType): Int

    /**
     *
     * Method: execute update by sql statement it is sql binding
     * @param sql include insert delete update
     * @param parameters
     * @return int
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeUpdateBySql(sql: String, parameters: Array<Any> = emptyArray()): Int

    /**
     *
     * Method: execute batch
     * @param sqls
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeBatch(sqls: Array<String>): IntArray

    /**
     *
     * Method: execute batch,transaction
     * @param sql include insert update delete sql only the same sql many data
     * @param parametersList
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeBatch(sql: String, parametersList: List<Array<Any>>): IntArray

    /**
     *
     * Method: execute batch
     * @param batchObjectCollection
     * @return int[]
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeBatch(batchObjectCollection: Collection<BaseQuery.BatchObject>): IntArray

    /**
     *
     * Method; get the total size,it is sql binding
     * @param <T>
     * @param table
     * @param condition
     * @param parameters
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> totalRows(table: String, condition: String = Constants.String.BLANK, parameters: Array<Any> = emptyArray()): Int

    /**
     *
     * Method; get the total size
     * @param <T>
     * @param clazz
     * @param table
     * @param condition
     * @param parameters
     * @return int
     * @throws QueryException
    </T> */
    @Throws(QueryException::class)
    fun <T : Any> totalRows(clazz: KClass<T>? = null, table: String = Constants.String.BLANK, condition: String = Constants.String.BLANK, parameters: Array<Any> = emptyArray()): Int

    /**
     *
     * execute transaction
     * @param transaction
     * @throws QueryException
     */
    @Throws(QueryException::class)
    fun executeTransaction(transaction: Transaction)
}
