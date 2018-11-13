package com.oneliang.ktx.frame.servlet.taglib

import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

import com.oneliang.Constants
import com.oneliang.util.common.StringUtil
import com.oneliang.util.common.TimeUtil
import com.oneliang.util.logging.Logger
import com.oneliang.util.logging.LoggerManager

class TimeMillisToDateTag : BodyTagSupport() {

    /**
     * @return the value
     */
    /**
     * @param value the value to set
     */
    var value: String? = null
    /**
     * @return the format
     */
    /**
     * @param format the format to set
     */
    var format = TimeUtil.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND

    @Throws(JspException::class)
    fun doStartTag(): Int {
        if (StringUtil.isNotBlank(this.value)) {
            try {
                val dateString = TimeUtil.dateToString(TimeUtil.timeMillisToDate(java.lang.Long.parseLong(value!!)), this.format)
                this.pageContext.getOut().print(dateString)
            } catch (e: Exception) {
                logger.error(Constants.Base.EXCEPTION, e)
            }

        }
        return EVAL_PAGE
    }

    @Throws(JspException::class)
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    companion object {

        /**
         * serialVersionUID
         */
        private val serialVersionUID = -4155763064911316797L

        private val logger = LoggerManager.getLogger(TimeMillisToDateTag::class.java)
    }

}
