package com.oneliang.ktx.frame.servlet.taglib

import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

import com.oneliang.Constants
import com.oneliang.frame.bean.Page
import com.oneliang.util.common.StringUtil
import com.oneliang.util.logging.Logger
import com.oneliang.util.logging.LoggerManager

class PaginationTag : BodyTagSupport() {

    /**
     * @return the action
     */
    /**
     * @param action the action to set
     */
    var action: String? = null
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
     * @return the size
     */
    /**
     * @param size the size to set
     */
    var size = 1
    /**
     * @return the firstIcon
     */
    /**
     * @param firstIcon the firstIcon to set
     */
    var firstIcon = "first"
    /**
     * @return the lastIcon
     */
    /**
     * @param lastIcon the lastIcon to set
     */
    var lastIcon = "last"
    /**
     * @return the previousIcon
     */
    /**
     * @param previousIcon the previousIcon to set
     */
    var previousIcon = "previous"
    /**
     * @return the nextIcon
     */
    /**
     * @param nextIcon the nextIcon to set
     */
    var nextIcon = "next"
    /**
     * @return the linkString
     */
    /**
     * @param linkString the linkString to set
     */
    var linkString: String? = null

    @Throws(JspException::class)
    fun doStartTag(): Int {
        this.action = StringUtil.nullToBlank(this.action)
        this.scope = StringUtil.nullToBlank(this.value)
        this.value = StringUtil.nullToBlank(this.scope)
        this.firstIcon = StringUtil.nullToBlank(this.firstIcon)
        this.lastIcon = StringUtil.nullToBlank(this.lastIcon)
        this.previousIcon = StringUtil.nullToBlank(this.previousIcon)
        this.nextIcon = StringUtil.nullToBlank(this.nextIcon)
        this.linkString = StringUtil.nullToBlank(this.linkString)
        return SKIP_BODY
    }

    @Throws(JspException::class)
    fun doEndTag(): Int {
        var `object`: Any? = null
        if (this.scope == Constants.RequestScope.SESSION) {
            `object` = this.pageContext.getSession().getAttribute(value)
        } else {
            `object` = this.pageContext.getRequest().getAttribute(value)
        }
        if (`object` is Page) {
            val page = `object` as Page?
            val paginationHtml = StringBuilder()
            var action: String? = null
            if (this.action!!.indexOf("?") > -1) {
                action = this.action!! + "&page="
            } else {
                action = this.action!! + "?page="
            }
            //first and previous
            val first = "<a href=\"" + action + page!!.getFirstPage() + "\" " + linkString + ">" + this.firstIcon + "</a>" + BLANK_2
            val previous = "<a href=\"" + action + (page!!.getPage() - 1) + "\" " + linkString + ">" + this.previousIcon + "</a>" + BLANK_2
            paginationHtml.append(first)
            paginationHtml.append(previous)
            //middle
            if (this.size > page!!.getTotalPages()) {
                this.size = page!!.getTotalPages()
            }
            var middlePosition = 0
            if (this.size % 2 == 0) {//even
                middlePosition = this.size / 2
            } else {//odd
                middlePosition = this.size / 2 + 1
            }
            var startPage = 0
            if (page!!.getPage() <= middlePosition) {
                startPage = 1
            } else if (page!!.getPage() > middlePosition) {
                if (page!!.getPage() > page!!.getTotalPages() - middlePosition) {
                    startPage = page!!.getTotalPages() - this.size + 1
                } else {
                    startPage = page!!.getPage() - middlePosition + 1
                }
            }
            for (i in 0 until this.size) {
                val showPage = startPage + i
                var middle: String? = null
                if (showPage == page!!.getPage()) {
                    middle = "<a href=\"$action$showPage\" $linkString><font color=\"red\">[$BLANK_2$showPage$BLANK_2]</font></a>$BLANK_2"
                } else {
                    middle = "<a href=\"$action$showPage\" $linkString>[$BLANK_2$showPage$BLANK_2]</a>$BLANK_2"
                }
                paginationHtml.append(middle)
            }
            //next and last(total)
            val next = "<a href=\"" + action + (page!!.getPage() + 1) + "\" " + linkString + ">" + this.nextIcon + "</a>" + BLANK_2
            val last = "<a href=\"" + action + page!!.getTotalPages() + "\" " + linkString + ">" + this.lastIcon + "</a>"
            paginationHtml.append(next)
            paginationHtml.append(last)
            val other = BLANK_8 + TIPS_PAGE + page!!.getPage() + "/" + page!!.getTotalPages() + BLANK_8 + TIPS_ROWS + (page!!.getPageFirstRow() + 1) + "~" + (if (page!!.getPage() * page!!.getRowsPerPage() < page!!.getTotalRows()) page!!.getPage() * page!!.getRowsPerPage() else page!!.getTotalRows()) + "/" + page!!.getTotalRows()
            paginationHtml.append(other)
            //goto page
            try {
                this.pageContext.getOut().println(paginationHtml.toString())
            } catch (e: Exception) {
                logger.error(Constants.Base.EXCEPTION, e)
            }

        }
        return EVAL_PAGE
    }

    companion object {

        /**
         * serialVersiionUID
         */
        private val serialVersionUID = -7387028073417293099L

        private val logger = LoggerManager.getLogger(PaginationTag::class.java)

        private val BLANK_2 = "&nbsp;&nbsp;"
        private val BLANK_8 = BLANK_2 + BLANK_2 + BLANK_2 + BLANK_2
        private val TIPS_PAGE = "Page:"
        private val TIPS_ROWS = "Rows:"
    }
}
