package com.oneliang.ktx.frame.servlet.taglib

import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

import com.oneliang.Constants
import com.oneliang.frame.bean.Page
import com.oneliang.util.common.StringUtil
import com.oneliang.util.common.TagUtil
import com.oneliang.util.logging.Logger
import com.oneliang.util.logging.LoggerManager

/**
 * @author Dandelion
 * @since 2008-11-06
 */
class TableBodyTag : BodyTagSupport() {

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
     * @return the wrapSize
     */
    /**
     * @param wrapSize the wrapSize to set
     */
    var wrapSize = 1
    /**
     * @return the trString
     */
    /**
     * @param trString the trString to set
     */
    var trString: String? = null
    /**
     * @return the paginationValue
     */
    /**
     * @param paginationValue the paginationValue to set
     */
    var paginationValue: String? = null
    /**
     * @return the paginationScope
     */
    /**
     * @param paginationScope the paginationScope to set
     */
    var paginationScope: String? = null

    /**
     *
     *
     * Method: override method do start tag
     *
     */
    @Throws(JspException::class)
    fun doStartTag(): Int {
        this.value = StringUtil.nullToBlank(this.value)
        this.scope = StringUtil.nullToBlank(this.scope)
        this.trString = StringUtil.nullToBlank(this.trString)
        this.paginationValue = StringUtil.nullToBlank(this.paginationValue)
        this.paginationScope = StringUtil.nullToBlank(this.paginationScope)
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
        val tdString = this.bodyContent.getString().trim()
        var o: Any? = null
        if (this.value != "") {
            if (this.scope == Constants.RequestScope.SESSION) {
                o = this.pageContext.getSession().getAttribute(this.value)
            } else {
                o = this.pageContext.getRequest().getAttribute(this.value)
            }
            val fields = TagUtil.fieldSplit(tdString)
            for (i in fields.indices) {
                fields[i] = StringUtil.trim(fields[i])
            }

            var currentPage = 1
            //pagination object
            var pageObject: Any? = null
            if (StringUtil.isNotBlank(this.paginationValue)) {
                if (this.paginationScope == Constants.RequestScope.SESSION) {
                    pageObject = this.pageContext.getSession().getAttribute(this.paginationValue)
                } else {
                    pageObject = this.pageContext.getRequest().getAttribute(this.paginationValue)
                }
                if (pageObject is Page) {
                    val page = pageObject as Page?
                    currentPage = page!!.getPage()
                }
            }

            if (o is List<*>) {
                val list = o as List<*>?
                val trs = StringBuilder()
                if (list != null && !list.isEmpty()) {
                    // delete blank

                    var count = 0
                    var index = Page.DEFAULT_ROWS * (currentPage - 1)
                    val total = list.size
                    for (`object` in list) {
                        var totalCells = count * fields.size
                        if (totalCells % this.wrapSize == 0) {
                            trs.append("<tr " + this.trString + ">")
                            index++
                        }
                        count++
                        trs.append(this.tdGenerator(fields, `object`, index))
                        totalCells = count * fields.size
                        if (totalCells % this.wrapSize == 0 || count == total) {
                            trs.append("</tr>")
                        }
                    }
                }
                try {
                    this.pageContext.getOut().print(trs.toString())
                } catch (e: Exception) {
                    logger.error(Constants.Base.EXCEPTION, e)
                }

            } else if (o is Any) {
                val trs = StringBuilder()
                trs.append("<tr " + this.trString + ">")
                trs.append(this.tdGenerator(fields, o, 0))
                trs.append("</tr>")
            }

        }
        return EVAL_PAGE
    }

    /**
     * generator the tds like<td>..</td><td>..</td>
     * @param fields
     * @param object
     * @param index
     * @return tdsstring
     */
    private fun tdGenerator(fields: Array<String>, `object`: Any?, index: Int): String {
        val tds = StringBuilder()
        for (td in fields) {
            val fieldStyle = TagUtil.fieldStyleSplit(td)
            tds.append("<td" + fieldStyle[1] + ">")
            if (fieldStyle[0] == "INDEX") {
                fieldStyle[0] = index.toString()
            } else {
                fieldStyle[0] = TagUtil.fieldReplace(fieldStyle[0], `object`, "no data")
            }
            tds.append(fieldStyle[0])
            tds.append("</td>")
        }
        return tds.toString()
    }

    companion object {
        /**
         * serialVersionUID
         */
        private val serialVersionUID = -7892161495680411808L

        private val logger = LoggerManager.getLogger(TableBodyTag::class.java)

        @JvmStatic
        fun main(arg: Array<String>) {
            //		String REGEX = "\\$\\{[\\w.]*\\}";

            //		String htmlString = "id=${ab.ad.b}&name=${name}";
            val REGEX = "FILTER\\[[\\S]*]."
            var htmlString = "FILTER[user.common?action=modify]..."
            htmlString = htmlString.replace(REGEX.toRegex(), "")
            println(htmlString)
        }
    }

}
