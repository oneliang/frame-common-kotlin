package com.oneliang.ktx.frame.servlet.taglib

import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

import com.oneliang.Constants
import com.oneliang.util.common.StringUtil
import com.oneliang.util.common.TagUtil
import com.oneliang.util.logging.Logger
import com.oneliang.util.logging.LoggerManager

/**
 * @author Dandelion
 * @since 2008-11-06
 */
class TableHeaderTag : BodyTagSupport() {

    /**
     * @return the trString
     */
    /**
     * @param trString the trString to set
     */
    var trString: String? = null

    /**
     *
     *
     * Method: override method do start tag
     *
     */
    @Throws(JspException::class)
    fun doStartTag(): Int {
        this.trString = StringUtil.nullToBlank(this.trString)
        val startTr = "<tr " + this.trString + ">"
        try {
            this.pageContext.getOut().print(startTr)
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
        }

        return EVAL_BODY_BUFFERED
    }

    /**
     *
     *
     * Method: override method do end tag
     *
     */
    @Throws(JspException::class)
    fun doEndTag(): Int {
        val thString = this.bodyContent.getString().trim()
        val headers = TagUtil.fieldSplit(thString)
        val ths = StringBuilder()
        for (th in headers) {
            th = StringUtil.trim(th)
            val fieldStyle = TagUtil.fieldStyleSplit(th)
            ths.append("<th")
            ths.append(fieldStyle[1])
            ths.append(">")
            ths.append(fieldStyle[0])
            ths.append("</th>")
        }
        val endTr = "</tr>"
        try {
            this.pageContext.getOut().print(ths.toString())
            this.pageContext.getOut().print(endTr)
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
        }

        return EVAL_PAGE
    }

    companion object {
        /**
         * serialVersionUID
         */
        private val serialVersionUID = 1390170675598454919L

        private val logger = LoggerManager.getLogger(TableHeaderTag::class.java)
    }
}
