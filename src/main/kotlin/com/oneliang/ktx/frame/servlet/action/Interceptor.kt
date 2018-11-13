package com.oneliang.ktx.frame.servlet.action

import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

interface Interceptor {
    /**
     * through intercept return true,else return false
     * @param request
     * @param response
     * @return boolean
     * @exception InterceptException
     */
    @Throws(Interceptor.InterceptException::class)
    fun doIntercept(request: ServletRequest, response: ServletResponse): Boolean

    class InterceptException : Exception {

        /**
         * @param message
         */
        constructor(message: String) : super(message) {}

        /**
         * @param cause
         */
        constructor(cause: Throwable) : super(cause) {}

        /**
         * @param message
         * @param cause
         */
        constructor(message: String, cause: Throwable) : super(message, cause) {}

        companion object {

            /**
             * serialVersionUID
             */
            private val serialVersionUID = 3607915711758608642L
        }
    }
}
