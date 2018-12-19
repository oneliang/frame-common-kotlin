package com.oneliang.ktx.frame.jdbc

import java.sql.Connection

/**
 * pagination query
 * @author Dandelion
 * @since 2008-11-25
 */
interface PaginationQuery {

    fun <T : Any> executeQueryLimit(connection: Connection, clazz: Class<T>): List<T>
}
