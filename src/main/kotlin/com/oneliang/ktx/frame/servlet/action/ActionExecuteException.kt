package com.oneliang.ktx.frame.servlet.action

class ActionExecuteException : Exception {

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
        private val serialVersionUID = -9142444524975582593L
    }
}
