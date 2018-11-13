package com.oneliang.ktx.frame.servlet.taglib

import java.util.Date
import java.util.Locale

import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

import com.oneliang.Constants
import com.oneliang.util.common.StringUtil
import com.oneliang.util.common.TimeUtil
import com.oneliang.util.logging.Logger
import com.oneliang.util.logging.LoggerManager

class DateFormatTag : BodyTagSupport() {

    /**
     * @return the value
     */
    /**
     * @param value the value to set
     */
    var value: String? = null
    /**
     * @return the originalFormat
     */
    /**
     * @param originalFormat the originalFormat to set
     */
    var originalFormat: String? = null
    /**
     * @return the format
     */
    /**
     * @param format the format to set
     */
    var format = TimeUtil.YEAR_MONTH_DAY
    /**
     * @return the originalLanguage
     */
    /**
     * @param originalLanguage the originalLanguage to set
     */
    var originalLanguage: String? = null
    /**
     * @return the language
     */
    /**
     * @param language the language to set
     */
    var language: String? = null
    /**
     * @return the originalCountry
     */
    /**
     * @param originalCountry the originalCountry to set
     */
    var originalCountry: String? = null
    /**
     * @return the country
     */
    /**
     * @param country the country to set
     */
    var country: String? = null

    /**
     * doStartTag
     */
    @Throws(JspException::class)
    fun doStartTag(): Int {
        if (StringUtil.isNotBlank(this.value)) {
            try {
                var originalLocale: Locale? = null
                if (StringUtil.isNotBlank(this.originalLanguage) && StringUtil.isNotBlank(this.originalCountry)) {
                    originalLocale = Locale(this.originalLanguage!!, this.originalCountry!!)
                } else if (StringUtil.isNotBlank(this.originalLanguage)) {
                    originalLocale = Locale(this.originalLanguage!!)
                } else {
                    originalLocale = Locale.getDefault()
                }
                var locale: Locale? = null
                if (StringUtil.isNotBlank(this.language) && StringUtil.isNotBlank(this.country)) {
                    locale = Locale(this.language!!, this.country!!)
                } else if (StringUtil.isNotBlank(this.language)) {
                    locale = Locale(this.language!!)
                } else {
                    locale = Locale.getDefault()
                }
                var originalFormat: String? = null
                if (StringUtil.isNotBlank(this.originalFormat)) {
                    originalFormat = this.originalFormat
                } else {
                    originalFormat = TimeUtil.DEFAULT_DATE_FORMAT
                }
                val date = TimeUtil.stringToDate(this.value, originalFormat, originalLocale)
                val dateString = TimeUtil.dateToString(date, this.format, locale)
                this.pageContext.getOut().print(dateString)
            } catch (e: Exception) {
                logger.error(Constants.Base.EXCEPTION, e)
            }

        }
        return EVAL_PAGE
    }

    companion object {

        /**
         * serialVersionUID
         */
        private val serialVersionUID = 3876792310864662550L

        private val logger = LoggerManager.getLogger(DateFormatTag::class.java)
    }
}
