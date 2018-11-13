package com.oneliang.ktx.frame.servlet.action

import java.util.concurrent.CopyOnWriteArrayList

import com.oneliang.Constants
import com.oneliang.frame.servlet.action.ActionInterface.HttpRequestMethod
import com.oneliang.ktx.util.common.StringUtil
import com.oneliang.util.common.StringUtil

open class ActionBean {

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
     * @return the path
     */
    /**
     * @param path the path to set
     */
    var path: String? = null
    /**
     * @return the httpRequestMethods
     */
    /**
     * @param httpRequestMethods the httpRequestMethods to set
     */
    var httpRequestMethods: String? = null
        set(httpRequestMethods) {
            field = httpRequestMethods
            if (StringUtil.isNotBlank(this.httpRequestMethods)) {
                this.httpRequestMethodsCode = 0
                val httpRequestMethodArray = this.httpRequestMethods!!.split(Constants.Symbol.COMMA.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                for (httpRequestMethod in httpRequestMethodArray) {
                    if (httpRequestMethod.equals(Constants.Http.RequestMethod.PUT, ignoreCase = true)) {
                        this.httpRequestMethodsCode = this.httpRequestMethodsCode or ActionInterface.HttpRequestMethod.PUT.getCode()
                    } else if (httpRequestMethod.equals(Constants.Http.RequestMethod.DELETE, ignoreCase = true)) {
                        this.httpRequestMethodsCode = this.httpRequestMethodsCode or ActionInterface.HttpRequestMethod.DELETE.getCode()
                    } else if (httpRequestMethod.equals(Constants.Http.RequestMethod.GET, ignoreCase = true)) {
                        this.httpRequestMethodsCode = this.httpRequestMethodsCode or ActionInterface.HttpRequestMethod.GET.getCode()
                    } else if (httpRequestMethod.equals(Constants.Http.RequestMethod.POST, ignoreCase = true)) {
                        this.httpRequestMethodsCode = this.httpRequestMethodsCode or ActionInterface.HttpRequestMethod.POST.getCode()
                    } else if (httpRequestMethod.equals(Constants.Http.RequestMethod.HEAD, ignoreCase = true)) {
                        this.httpRequestMethodsCode = this.httpRequestMethodsCode or HttpRequestMethod.HEAD.getCode()
                    } else if (httpRequestMethod.equals(Constants.Http.RequestMethod.OPTIONS, ignoreCase = true)) {
                        this.httpRequestMethodsCode = this.httpRequestMethodsCode or HttpRequestMethod.OPTIONS.getCode()
                    } else if (httpRequestMethod.equals(Constants.Http.RequestMethod.TRACE, ignoreCase = true)) {
                        this.httpRequestMethodsCode = this.httpRequestMethodsCode or HttpRequestMethod.TRACE.getCode()
                    }
                }
            }
        }
    /**
     * @return the httpRequestMethodsCode
     */
    var httpRequestMethodsCode = ActionInterface.HttpRequestMethod.GET.getCode() or ActionInterface.HttpRequestMethod.POST.getCode()
        private set
    /**
     * @return the actionInstance
     */
    /**
     * @param actionInstance the actionInstance to set
     */
    var actionInstance: Any? = null
    private val actionInterceptorBeanList = CopyOnWriteArrayList<ActionInterceptorBean>()
    private val beforeActionInterceptorBeanList = CopyOnWriteArrayList<ActionInterceptorBean>()
    private val afterActionInterceptorBeanList = CopyOnWriteArrayList<ActionInterceptorBean>()
    private val actionForwardBeanList = CopyOnWriteArrayList<ActionForwardBean>()

    /**
     * find forward path
     * @param forward
     * @return forward path
     */
    fun findForwardPath(forward: String): String? {
        var forwardPath: String? = null
        for (actionForwardBean in actionForwardBeanList) {
            val forwardName = actionForwardBean.name
            if (forwardName != null && forwardName == forward) {
                forwardPath = actionForwardBean.path
                break
            }
        }
        return forwardPath
    }

    /**
     * find action forward bean by static parameter
     * @param parameterMap
     * @return boolean
     */
    fun findActionForwardBeanByStaticParameter(parameterMap: Map<String, Array<String>>): ActionForwardBean? {
        var forwardBean: ActionForwardBean? = null
        for (actionForwardBean in actionForwardBeanList) {
            if (actionForwardBean.isContainsParameters(parameterMap)) {
                forwardBean = actionForwardBean.clone()
                this.replaceActionForwardBeanStaticFilePath(forwardBean, parameterMap)
                break
            }
        }
        return forwardBean
    }

    /**
     * replace action forward bean static file path
     * @param actionForwardBean
     * @param parameterMap
     */
    private fun replaceActionForwardBeanStaticFilePath(actionForwardBean: ActionForwardBean?, parameterMap: Map<String, Array<String>>?) {
        if (actionForwardBean != null && parameterMap != null) {
            val staticFilePath = actionForwardBean.staticFilePath
            var staticFilePathResult = staticFilePath
            val groupList = StringUtil.parseStringGroup(staticFilePath, REGEX, FIRST_REGEX, StringUtil.BLANK, 1)
            if (groupList != null) {
                for (group in groupList!!) {
                    val parameterValues = parameterMap[group]
                    if (parameterValues != null && parameterValues.size > 0) {
                        staticFilePathResult = staticFilePathResult!!.replaceFirst(REGEX.toRegex(), parameterValues[0])
                    }
                }
            }
            actionForwardBean.staticFilePath = staticFilePathResult
        }
    }

    /**
     * addInterceptor
     * @param actionInterceptorBean
     */
    fun addActionBeanInterceptor(actionInterceptorBean: ActionInterceptorBean?) {
        if (actionInterceptorBean != null) {
            val interceptorMode = actionInterceptorBean.mode
            if (interceptorMode != null) {
                if (interceptorMode == ActionInterceptorBean.INTERCEPTOR_MODE_BEFORE) {
                    this.beforeActionInterceptorBeanList.add(actionInterceptorBean)
                } else if (interceptorMode == ActionInterceptorBean.INTERCEPTOR_MODE_AFTER) {
                    this.afterActionInterceptorBeanList.add(actionInterceptorBean)
                }
            }
            this.actionInterceptorBeanList.add(actionInterceptorBean)
        }
    }

    /**
     * @return the action interceptor bean list
     */
    fun getActionInterceptorBeanList(): List<ActionInterceptorBean> {
        return this.actionInterceptorBeanList
    }

    /**
     * add action forward bean
     * @param actionForwardBean
     * @return boolean
     */
    fun addActionForwardBean(actionForwardBean: ActionForwardBean): Boolean {
        return this.actionForwardBeanList.add(actionForwardBean)
    }

    /**
     * @return the forwardList
     */
    fun getActionForwardBeanList(): List<ActionForwardBean> {
        return actionForwardBeanList
    }

    /**
     * @return the beforeInterceptorBeanList
     */
    fun getBeforeActionInterceptorBeanList(): List<ActionInterceptorBean> {
        return beforeActionInterceptorBeanList
    }

    /**
     * @return the afterInterceptorBeanList
     */
    fun getAfterActionInterceptorBeanList(): List<ActionInterceptorBean> {
        return afterActionInterceptorBeanList
    }

    /**
     * is contain http request method
     * @param httpRequestMethod
     * @return boolean
     */
    fun isContainHttpRequestMethod(httpRequestMethod: HttpRequestMethod?): Boolean {
        var result = false
        if (httpRequestMethod != null) {
            result = if (httpRequestMethod!!.getCode() === this.httpRequestMethodsCode and httpRequestMethod!!.getCode()) true else false
        }
        return result
    }

    companion object {

        private val REGEX = "\\{[\\w]*\\}"
        private val FIRST_REGEX = "\\{"

        val TAG_ACTION = "action"
    }
}
