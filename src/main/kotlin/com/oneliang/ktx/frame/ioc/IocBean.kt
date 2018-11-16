package com.oneliang.ktx.frame.ioc

import com.oneliang.ktx.Constants

class IocBean {
    companion object {
        const val TAG_BEAN = "bean"
        const val INJECT_TYPE_AUTO_BY_TYPE = "autoByType"
        const val INJECT_TYPE_AUTO_BY_ID = "autoById"
        const val INJECT_TYPE_MANUAL = "manual"
    }
    /**
     * @return the id
     */
    /**
     * @param id the id to set
     */
    var id: String = Constants.String.BLANK
    /**
     * @return the type
     */
    /**
     * @param type the type to set
     */
    var type: String = Constants.String.BLANK
    /**
     * @return the value
     */
    /**
     * @param value the value to set
     */
    var value: String = Constants.String.BLANK
    /**
     * @return the proxy
     */
    /**
     * @param proxy the proxy to set
     */
    var proxy = true
    /**
     * @return the injectType
     */
    /**
     * @param injectType the injectType to set
     */
    var injectType = INJECT_TYPE_AUTO_BY_ID
    /**
     * @return the beanClass
     */
    /**
     * @param beanClass the beanClass to set
     */
    var beanClass: Class<*>? = null
    /**
     * @return the beanInstance
     */
    /**
     * @param beanInstance the beanInstance to set
     */
    var beanInstance: Any? = null
    /**
     * @return the proxyInstance
     */
    /**
     * @param proxyInstance the proxyInstance to set
     */
    var proxyInstance: Any? = null
    /**
     * @return the iocConstructorBean
     */
    /**
     * @param iocConstructorBean the iocConstructorBean to set
     */
    var iocConstructorBean: IocConstructorBean? = null
    val iocPropertyBeanList = mutableListOf<IocPropertyBean>()
    val iocAfterInjectBeanList = mutableListOf<IocAfterInjectBean>()

    /**
     * @param iocPropertyBean
     * @return boolean
     */
    fun addIocPropertyBean(iocPropertyBean: IocPropertyBean): Boolean {
        return iocPropertyBeanList.add(iocPropertyBean)
    }

    /**
     * @param iocAfterInjectBean
     * @return boolean
     */
    fun addIocAfterInjectBean(iocAfterInjectBean: IocAfterInjectBean): Boolean {
        return iocAfterInjectBeanList.add(iocAfterInjectBean)
    }
}
