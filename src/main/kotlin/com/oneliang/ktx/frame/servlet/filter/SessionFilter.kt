package com.oneliang.ktx.frame.servlet.filter

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.Encoder
import com.oneliang.ktx.util.common.matchPattern
import java.io.IOException
import javax.servlet.*
import javax.servlet.http.HttpServletRequest

/**
 * session filter
 * @author Dandelion
 * @since 2010-06-27
 */
class SessionFilter : Filter {
    companion object {

        private const val SESSION_KEY = "sessionKey"
        private const val EXCLUDE_PATH = "excludePath"
        private const val ERROR_FORWARD = "errorForward"
        private const val COMMA_SPLIT = ","

        private val QUESTION_ENCODE = Encoder.escape("?")
        private val EQUAL_ENCODE = Encoder.escape("=")
        private val AND_ENCODE = Encoder.escape("&")
    }

    private var sessionKeyArray: Array<String> = emptyArray()
    private var excludePathArray: Array<String> = emptyArray()
    private var errorForward: String = Constants.String.BLANK

    /**
     * initial from config file
     */
    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        val sessionKeys = filterConfig.getInitParameter(SESSION_KEY)
        val excludePaths = filterConfig.getInitParameter(EXCLUDE_PATH)
        this.errorForward = filterConfig.getInitParameter(ERROR_FORWARD)
        if (sessionKeys != null) {
            val sessionKeyArray = sessionKeys.split(COMMA_SPLIT)
            this.sessionKeyArray = Array(sessionKeyArray.size) { Constants.String.BLANK }
            for ((i, sessionKey) in sessionKeyArray.withIndex()) {
                this.sessionKeyArray[i] = sessionKey.trim()
            }
        }
        if (excludePaths != null) {
            val excludePathArray = excludePaths.split(COMMA_SPLIT)
            this.excludePathArray = Array(excludePathArray.size) { Constants.String.BLANK }
            for ((i, excludePath) in excludePathArray.withIndex()) {
                this.excludePathArray[i] = excludePath.trim()
            }
        }
    }

    /**
     * sessionFilter
     */
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse,
                          filterChain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpSession = httpRequest.getSession()
        val webRoot = httpRequest.getContextPath()
        val requestUri = httpRequest.getRequestURI()
        var excludePathThrough = false
        var sessionThrough = false
        if (this.excludePathArray.isNotEmpty()) {
            for (excludePath in this.excludePathArray) {
                val path = webRoot + excludePath
                if (requestUri.matchPattern(path)) {
                    excludePathThrough = true
                    break
                }
            }
        }
        if (this.sessionKeyArray.isNotEmpty()) {
            for (sessionKey in this.sessionKeyArray) {
                val instance = httpSession.getAttribute(sessionKey)
                if (instance != null) {
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
            var uri = httpRequest.requestURI
            val front = httpRequest.contextPath.length
            uri = uri.substring(front)
            val params = mapToParameter(request.getParameterMap())
            var errorForwardUrl = this.errorForward
            if (errorForwardUrl.indexOf(Constants.Symbol.QUESTION_MARK) > 0) {
                errorForwardUrl = this.errorForward + Constants.Symbol.AND + Constants.RequestParameter.RETURN_URL + Constants.Symbol.EQUAL + uri + QUESTION_ENCODE + params
            } else {
                errorForwardUrl = this.errorForward + Constants.Symbol.QUESTION_MARK + Constants.RequestParameter.RETURN_URL + Constants.Symbol.EQUAL + uri + QUESTION_ENCODE + params
            }
            httpRequest.getRequestDispatcher(errorForwardUrl).forward(request, response)
        }
    }

    private fun mapToParameter(map: Map<String, Array<String>>): String {
        val params = StringBuilder()
        val iterator = map.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val key = entry.key
            val values = entry.value
            for ((j, value) in values.withIndex()) {
                params.append(key + EQUAL_ENCODE + value)
                if (iterator.hasNext() || j < values.size - 1) {
                    params.append(AND_ENCODE)
                }
            }
        }
        return params.toString()
    }

    override fun destroy() {
        this.sessionKeyArray = emptyArray()
        this.excludePathArray = emptyArray()
        this.errorForward = Constants.String.BLANK
    }
}
