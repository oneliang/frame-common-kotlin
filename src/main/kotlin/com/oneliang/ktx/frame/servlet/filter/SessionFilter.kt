package com.oneliang.ktx.frame.servlet.filter

import java.io.IOException
import kotlin.collections.Map.Entry

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

import com.oneliang.Constants
import com.oneliang.util.common.Encoder
import com.oneliang.util.common.StringUtil

/**
 * session filter
 * @author Dandelion
 * @since 2010-06-27
 */
class SessionFilter : Filter {

    private var sessionKeyArray: Array<String>? = null
    private var excludePathArray: Array<String>? = null
    private var errorForward: String? = null

    /**
     * initial from config file
     */
    @Throws(ServletException::class)
    fun init(filterConfig: FilterConfig) {
        val sessionKeys = filterConfig.getInitParameter(SESSION_KEY)
        val excludePaths = filterConfig.getInitParameter(EXCLUDE_PATH)
        this.errorForward = filterConfig.getInitParameter(ERROR_FORWARD)
        if (sessionKeys != null) {
            val sessionKeyArray = sessionKeys!!.split(COMMA_SPLIT.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            this.sessionKeyArray = arrayOfNulls(sessionKeyArray.size)
            var i = 0
            for (sessionKey in sessionKeyArray) {
                this.sessionKeyArray[i] = sessionKey.trim({ it <= ' ' })
                i++
            }
        }
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
     * sessionFilter
     */
    @Throws(IOException::class, ServletException::class)
    fun doFilter(request: ServletRequest, response: ServletResponse,
                 filterChain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpSession = httpRequest.getSession()
        val webRoot = httpRequest.getContextPath()
        val requestUri = httpRequest.getRequestURI()
        var excludePathThrough = false
        var sessionThrough = false
        if (this.excludePathArray != null) {
            for (excludePath in this.excludePathArray!!) {
                val path = webRoot + excludePath
                if (StringUtil.isMatchPattern(requestUri, path)) {
                    excludePathThrough = true
                    break
                }
            }
        }
        if (this.sessionKeyArray != null) {
            for (sessionKey in this.sessionKeyArray!!) {
                val `object` = httpSession.getAttribute(sessionKey)
                if (`object` != null) {
                    sessionThrough = true
                    break
                }
            }
        }
        if (excludePathThrough) {
            filterChain.doFilter(request, response)
        } else if (sessionThrough) {
            filterChain.doFilter(request, response)
        } else {
            var uri = httpRequest.getRequestURI()
            val front = httpRequest.getContextPath().length()
            uri = uri.substring(front)
            val params = mapToParameter(request.getParameterMap())
            var errorForwardUrl = this.errorForward
            if (errorForwardUrl!!.indexOf(Constants.Symbol.QUESTION_MARK) > 0) {
                errorForwardUrl = this.errorForward + Constants.Symbol.AND + Constants.RequestParameter.RETURN_URL + Constants.Symbol.EQUAL + uri + QUESTION_ENCODE + params
            } else {
                errorForwardUrl = this.errorForward + Constants.Symbol.QUESTION_MARK + Constants.RequestParameter.RETURN_URL + Constants.Symbol.EQUAL + uri + QUESTION_ENCODE + params
            }
            httpRequest.getRequestDispatcher(errorForwardUrl).forward(request, response)
        }
    }

    private fun mapToParameter(map: Map<String, Array<String>>?): String {
        val params = StringBuilder()
        if (map != null) {
            val iterator = map.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val key = entry.key
                val values = entry.value
                var j = 0
                for (value in values) {
                    params.append(key + EQUAL_ENCODE + value)
                    if (iterator.hasNext() || j < values.size - 1) {
                        params.append(AND_ENCODE)
                    }
                    j++
                }
            }
        }
        return params.toString()
    }

    fun destroy() {
        this.sessionKeyArray = null
        this.excludePathArray = null
        this.errorForward = null
    }

    companion object {

        private val SESSION_KEY = "sessionKey"
        private val EXCLUDE_PATH = "excludePath"
        private val ERROR_FORWARD = "errorForward"
        private val COMMA_SPLIT = ","

        private val QUESTION_ENCODE = Encoder.escape("?")
        private val EQUAL_ENCODE = Encoder.escape("=")
        private val AND_ENCODE = Encoder.escape("&")
    }
}
