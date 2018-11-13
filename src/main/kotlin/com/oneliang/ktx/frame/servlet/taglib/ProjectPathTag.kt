package com.oneliang.ktx.frame.servlet.taglib

import javax.servlet.http.HttpServletRequest
import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

import com.oneliang.Constants
import com.oneliang.util.logging.Logger
import com.oneliang.util.logging.LoggerManager

class ProjectPathTag : BodyTagSupport() {

    /**
     * doStartTag
     */
    @Throws(JspException::class)
    fun doStartTag(): Int {
        val request = pageContext.getRequest() as HttpServletRequest
        val path = request.getContextPath()
        try {
            pageContext.getOut().print(path)
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
        }

        return EVAL_PAGE
    }

    companion object {

        /**
         * serialVersionUID
         */
        private val serialVersionUID = -1688901633941253335L

        private val logger = LoggerManager.getLogger(ProjectPathTag::class.java)
    }
}
