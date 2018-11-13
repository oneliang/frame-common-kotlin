package com.oneliang.ktx.frame.servlet.taglib

import javax.servlet.http.HttpServletRequest
import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

import com.oneliang.Constants
import com.oneliang.util.logging.Logger
import com.oneliang.util.logging.LoggerManager

class BasePathTag : BodyTagSupport() {

    /**
     * doStartTag
     */
    @Throws(JspException::class)
    fun doStartTag(): Int {
        val request = pageContext.getRequest() as HttpServletRequest
        val path = request.getContextPath()
        val basePath = request.getScheme() + Constants.Symbol.COLON + Constants.Symbol.SLASH_LEFT + Constants.Symbol.SLASH_LEFT + request.getServerName() + Constants.Symbol.COLON + request.getServerPort() + path + Constants.Symbol.SLASH_LEFT
        try {
            pageContext.getOut().print(basePath)
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
        }

        return EVAL_PAGE
    }

    companion object {

        /**
         * serialVersionUID
         */
        private val serialVersionUID = 8252008854114747305L

        private val logger = LoggerManager.getLogger(BasePathTag::class.java)
    }
}
