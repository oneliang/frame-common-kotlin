package com.oneliang.ktx.frame.servlet.taglib

import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

import com.oneliang.Constants
import com.oneliang.util.common.StringUtil
import com.oneliang.util.logging.Logger
import com.oneliang.util.logging.LoggerManager

/**
 * @author Dandelion
 * @since 2008-11-06
 */
class TableTag : BodyTagSupport() {

    /**
     * @return the tableString
     */
    /**
     * @param tableString the tableString to set
     */
    var tableString: String? = null

    /**
     *
     *
     * Method: override method do start tag
     *
     */
    @Throws(JspException::class)
    fun doStartTag(): Int {
        this.tableString = StringUtil.nullToBlank(this.tableString)
        val startTable = "<table " + this.tableString + ">"
        try {
            this.pageContext.getOut().print(startTable)
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
        }

        return EVAL_BODY_INCLUDE
    }

    /**
     *
     *
     * Method: override method do end tag
     *
     */
    @Throws(JspException::class)
    fun doEndTag(): Int {
        val endTable = "</table>"
        try {
            this.pageContext.getOut().print(endTable)
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
        }

        return EVAL_PAGE
    }

    companion object {

        /**
         * serialVersionUID
         */
        private val serialVersionUID = 3944273586211542011L

        private val logger = LoggerManager.getLogger(TableTag::class.java)
    }
}
