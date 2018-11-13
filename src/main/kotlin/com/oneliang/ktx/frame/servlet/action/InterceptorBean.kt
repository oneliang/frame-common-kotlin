package com.oneliang.ktx.frame.servlet.action


class InterceptorBean {

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
     * @return the interceptorInstance
     */
    /**
     * @param interceptorInstance the interceptorInstance to set
     */
    var interceptorInstance: Interceptor? = null

    companion object {

        val TAG_INTERCEPTOR = "interceptor"
    }
}
