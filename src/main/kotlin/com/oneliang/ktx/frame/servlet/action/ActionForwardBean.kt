package com.oneliang.ktx.frame.servlet.action

import kotlin.collections.Map.Entry
import java.util.concurrent.ConcurrentHashMap

import com.oneliang.util.common.RequestUtil
import com.oneliang.util.common.StringUtil

class ActionForwardBean : Cloneable {

    /**
     * @return the name
     */
    /**
     * @param name the name to set
     */
    var name: String? = null
    /**
     * @return the path
     */
    /**
     * @param path the path to set
     */
    var path: String? = null
    /**
     * @return the staticParameters
     */
    /**
     * @param staticParameters the staticParameters to set
     */
    var staticParameters: String? = null
        set(staticParameters) {
            field = staticParameters
            if (this.staticParameters != null) {
                val parameterMap = RequestUtil.parseParameterString(this.staticParameters)
                this.parameterMap.putAll(parameterMap)
            }
        }
    /**
     * @return the staticFilePath
     */
    /**
     * @param staticFilePath the staticFilePath to set
     */
    var staticFilePath: String? = null
    private val parameterMap = ConcurrentHashMap<String, Array<String>>()

    /**
     * is contains parameters
     * @param parameterMap
     * @return boolean
     */
    fun isContainsParameters(parameterMap: Map<String, Array<String>>?): Boolean {
        var result = true
        if (parameterMap != null) {
            if (!this.parameterMap.isEmpty()) {
                val iterator = this.parameterMap.entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    val settingParameterKey = entry.key
                    if (parameterMap.containsKey(settingParameterKey)) {
                        val settingParameterValues = entry.value
                        val parameterValues = parameterMap[settingParameterKey]
                        if (settingParameterValues != null && parameterValues != null && settingParameterValues.size > 0 && parameterValues.size > 0) {
                            if (!StringUtil.isMatchPattern(parameterValues[0], settingParameterValues[0])) {
                                result = false
                            }
                        }
                    } else {
                        result = false
                    }
                    if (!result) {
                        break
                    }
                }
            } else {
                result = false
            }
        }
        return result
    }

    /**
     * clone action forward bean
     */
    public override fun clone(): ActionForwardBean {
        val actionForwardBean = ActionForwardBean()
        actionForwardBean.name = this.name
        actionForwardBean.path = this.path
        actionForwardBean.staticParameters = this.staticParameters
        actionForwardBean.staticFilePath = this.staticFilePath
        return actionForwardBean
    }

    /**
     * @return the parameterMap
     */
    fun getParameterMap(): Map<String, Array<String>> {
        return parameterMap
    }

    companion object {

        val TAG_FORWARD = "forward"
    }
}
