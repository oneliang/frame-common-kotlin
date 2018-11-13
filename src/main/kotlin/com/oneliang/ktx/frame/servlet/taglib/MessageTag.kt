package com.oneliang.ktx.frame.servlet.taglib

import java.util.Locale
import java.util.Properties

import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

import com.oneliang.Constants
import com.oneliang.frame.i18n.MessageContext
import com.oneliang.util.common.StringUtil
import com.oneliang.util.logging.Logger
import com.oneliang.util.logging.LoggerManager

class MessageTag : BodyTagSupport() {

    /**
     * @return the key
     */
    /**
     * @param key the key to set
     */
    var key: String? = null
    /**
     * @return the locale
     */
    /**
     * @param locale the locale to set
     */
    var locale: String? = null

    @Throws(JspException::class)
    fun doStartTag(): Int {
        return SKIP_BODY
    }

    @Throws(JspException::class)
    fun doEndTag(): Int {
        try {
            var locale: String? = null
            val localeParameter = this.pageContext.getRequest().getParameter(LOCALE)
            val localeKey = Locale.getDefault().toString()
            if (StringUtil.isBlank(this.locale)) {
                if (StringUtil.isBlank(localeParameter)) {
                    locale = localeKey
                } else {
                    locale = localeParameter
                }
            } else {
                locale = this.locale
            }
            val properties = MessageContext.getMessageProperties(locale)
            var value = this.key
            if (properties != null) {
                value = properties!!.getProperty(this.key!!)
            }
            this.pageContext.getOut().print(value)
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
        }

        return EVAL_PAGE
    }

    companion object {

        /**
         * serialVersionUID
         */
        private val serialVersionUID = -4127533235059676726L

        private val logger = LoggerManager.getLogger(MessageTag::class.java)

        private val LOCALE = "locale"
    }
}
