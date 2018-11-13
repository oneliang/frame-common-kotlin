package com.oneliang.ktx.frame.servlet.filter

import java.io.IOException

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

import com.oneliang.Constants

/**
 *
 *
 * Class: EncodingFilter class
 *
 *
 * com.lwx.frame.filter.EncodingFilter.java
 * This is a encoding filter in commonFrame
 * @author Dandelion
 * @since 2008-07-31
 */
class EncodingFilter : Filter {

    private var encoding: String? = null
    private var filterConfig: FilterConfig? = null
    private var ignore = false

    /**
     *
     * Method: public void destory()
     * This method will be reset the encoding=null and filterconfig=null
     */
    fun destroy() {
        this.encoding = null
        this.filterConfig = null
    }

    /**
     *
     * Method: public void doFilter(ServletRequest,ServletResponse,FilterChain) throws IOException,ServletException
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException,ServletException
     * This method will be doFilter in request scope and response scope
     */
    @Throws(IOException::class, ServletException::class)
    fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        if (!this.ignore) {
            servletRequest.setCharacterEncoding(this.encoding)
            servletResponse.setCharacterEncoding(this.encoding)
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse)
        } catch (sx: ServletException) {
            this.filterConfig!!.getServletContext().log(sx.getMessage())
        } catch (iox: IOException) {
            this.filterConfig!!.getServletContext().log(iox.message)
        }

    }

    /**
     *
     * Method: public void init(FilterConfig filterConfig) throws ServletException
     * @param filterConfig
     * @throws ServletException
     * This method will be initial the key 'encoding' and 'ignore' in web.xml
     */
    @Throws(ServletException::class)
    fun init(filterConfig: FilterConfig) {
        this.filterConfig = filterConfig
        // read from web.xml to initial the key 'encoding' and 'ignore'
        val encoding = filterConfig.getInitParameter(ENCODING)
        val ignore = filterConfig.getInitParameter(IGNORE)
        if (encoding == null) {
            this.encoding = DEFAULT_ENCODING
        } else {
            this.encoding = encoding
        }
        if (ignore == null)
            this.ignore = false
        else if (ignore!!.equals("true", ignoreCase = true))
            this.ignore = true
        else if (ignore!!.equals("yes", ignoreCase = true))
            this.ignore = true
        else
            this.ignore = false
    }

    companion object {

        private val DEFAULT_ENCODING = Constants.Encoding.UTF8
        private val ENCODING = "encoding"
        private val IGNORE = "ignore"
    }
}
