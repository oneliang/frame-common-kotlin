package com.oneliang.ktx.frame.servlet.taglib

import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

import com.oneliang.Constants
import com.oneliang.util.common.StringUtil

class IfEmptyTag : BodyTagSupport() {

    /**
     * @return the value
     */
    /**
     * @param value the value to set
     */
    var value: String? = null
    /**
     * @return the scope
     */
    /**
     * @param scope the scope to set
     */
    var scope: String? = null

    /**
     * doStartTag
     */
    @Throws(JspException::class)
    fun doStartTag(): Int {
        this.value = StringUtil.nullToBlank(this.value)
        this.scope = StringUtil.nullToBlank(this.scope)
        var o: Any? = null
        if (this.scope == Constants.RequestScope.SESSION) {
            o = this.pageContext.getSession().getAttribute(this.value)
        } else {
            o = this.pageContext.getRequest().getAttribute(this.value)
        }
        var eval = EVAL_PAGE
        if (o == null) {
            eval = EVAL_BODY_INCLUDE
        } else {
            if (o is List<*>) {
                val list = o as List<*>?
                if (list!!.isEmpty()) {
                    eval = EVAL_BODY_INCLUDE
                }
            }
        }
        return eval
    }

    /**
     * doEndTag
     */
    @Throws(JspException::class)
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    companion object {

        /**
         * serialVersionUID
         */
        private val serialVersionUID = 300829271738694544L
    }
}
