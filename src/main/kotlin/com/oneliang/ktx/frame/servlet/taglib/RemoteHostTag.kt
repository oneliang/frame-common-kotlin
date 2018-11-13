package com.oneliang.ktx.frame.servlet.taglib

import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

import com.oneliang.Constants
import com.oneliang.util.logging.Logger
import com.oneliang.util.logging.LoggerManager

class RemoteHostTag : BodyTagSupport() {

    /**
     * doStartTag
     */
    @Throws(JspException::class)
    fun doStartTag(): Int {
        val remoteHost = pageContext.getRequest().getRemoteHost()
        try {
            pageContext.getOut().print(remoteHost)
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
        }

        return EVAL_PAGE
    }

    companion object {

        /**
         * serialVersionUID
         */
        private val serialVersionUID = 4623150037980794467L

        private val logger = LoggerManager.getLogger(RemoteHostTag::class.java)
    }
}