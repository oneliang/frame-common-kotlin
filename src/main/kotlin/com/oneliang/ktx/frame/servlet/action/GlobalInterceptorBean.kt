package com.oneliang.ktx.frame.servlet.action


class GlobalInterceptorBean {

    /**
     * @return the id
     */
    /**
     * @param id the id to set
     */
    var id: String? = null
    /**
     * @return the type
     */
    /**
     * @param type the type to set
     */
    var type: String? = null
    /**
     * @return the mode
     */
    /**
     * @param mode the mode to set
     */
    var mode: String? = null
    /**
     * @return the interceptorInstance
     */
    /**
     * @param interceptorInstance the interceptorInstance to set
     */
    var interceptorInstance: Interceptor? = null

    companion object {

        val TAG_GLOBAL_INTERCEPTOR = "global-interceptor"

        val INTERCEPTOR_MODE_BEFORE = "before"
        val INTERCEPTOR_MODE_AFTER = "after"
    }
}
