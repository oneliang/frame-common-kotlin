package com.oneliang.ktx.frame.jdbc

import java.sql.Connection

object TransactionManager {

    internal val customTransactionSign = ThreadLocal<Boolean>()
    internal val customTransactionConnection = ThreadLocal<Connection>()
}
