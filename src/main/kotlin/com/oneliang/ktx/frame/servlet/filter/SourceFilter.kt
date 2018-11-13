package com.oneliang.ktx.frame.servlet.filter

import java.io.IOException

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

import com.oneliang.util.common.StringUtil

/**
 * source filter
 * @author Dandelion
 * @since 2010-06-27
 */
class SourceFilter : Filter {

    private var excludePathArray: Array<String>? = null
    private var errorForward: String? = null

    /**
     * initial from config file
     */
    @Throws(ServletException::class)
    fun init(filterConfig: FilterConfig) {
        val excludePaths = filterConfig.getInitParameter(EXCLUDE_PATH)
        this.errorForward = filterConfig.getInitParameter(ERROR_FORWARD)
        if (excludePaths != null) {
            val excludePathArray = excludePaths!!.split(COMMA_SPLIT.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            this.excludePathArray = arrayOfNulls(excludePathArray.size)
            var i = 0
            for (excludePath in excludePathArray) {
                this.excludePathArray[i] = excludePath.trim({ it <= ' ' })
                i++
            }
        }
    }

    /**
     * do filter
     */
    @Throws(IOException::class, ServletException::class)
    fun doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        //		HttpServletResponse httpResponse = (HttpServletResponse)response;
        //		HttpSession session = httpRequest.getSession();
        //		httpResponse.setHeader("Cache-Control","no-cache");
        //		httpResponse.setHeader("Pragma","no-cache");
        //		httpResponse.setDateHeader ("Expires", -1);
        val projectPath = httpRequest.getContextPath()
        val requestUri = httpRequest.getRequestURI()
        var excludePathThrough = false
        if (this.excludePathArray != null) {
            for (excludePath in this.excludePathArray!!) {
                val path = projectPath + excludePath
                if (StringUtil.isMatchPattern(requestUri, path)) {
                    excludePathThrough = true
                    break
                }
            }
        }
        if (excludePathThrough) {
            filterChain.doFilter(request, response)
        } else {
            httpRequest.getRequestDispatcher(this.errorForward).forward(request, response)
        }
    }

    fun destroy() {
        this.excludePathArray = null
        this.errorForward = null
    }

    companion object {

        private val EXCLUDE_PATH = "excludePath"
        private val ERROR_FORWARD = "errorForward"
        private val COMMA_SPLIT = ","
    }
}
