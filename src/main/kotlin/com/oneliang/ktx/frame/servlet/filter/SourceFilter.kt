package com.oneliang.ktx.frame.servlet.filter

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.matchPattern
import java.io.IOException
import javax.servlet.*
import javax.servlet.http.HttpServletRequest

/**
 * source filter
 * @author Dandelion
 * @since 2010-06-27
 */
class SourceFilter : Filter {

    companion object {
        private const val EXCLUDE_PATH = "excludePath"
        private const val ERROR_FORWARD = "errorForward"
        private const val COMMA_SPLIT = ","
    }

    private var excludePathArray: Array<String> = emptyArray()
    private var errorForward: String = Constants.String.BLANK

    /**
     * initial from config file
     */
    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        val excludePaths = filterConfig.getInitParameter(EXCLUDE_PATH)
        this.errorForward = filterConfig.getInitParameter(ERROR_FORWARD)
        if (excludePaths != null) {
            val excludePathArray = excludePaths.split(COMMA_SPLIT)
            this.excludePathArray = Array(excludePathArray.size) { Constants.String.BLANK }
            for ((i, excludePath) in excludePathArray.withIndex()) {
                this.excludePathArray[i] = excludePath.trim()
            }
        }
    }

    /**
     * do filter
     */
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        //		HttpServletResponse httpResponse = (HttpServletResponse)response;
        //		HttpSession session = httpRequest.getSession();
        //		httpResponse.setHeader("Cache-Control","no-cache");
        //		httpResponse.setHeader("Pragma","no-cache");
        //		httpResponse.setDateHeader ("Expires", -1);
        val projectPath = httpRequest.getContextPath()
        val requestUri = httpRequest.getRequestURI()
        var excludePathThrough = false
        if (this.excludePathArray.isNotEmpty()) {
            for (excludePath in this.excludePathArray) {
                val path = projectPath + excludePath
                if (requestUri.matchPattern(path)) {
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

    override fun destroy() {
        this.excludePathArray = emptyArray()
        this.errorForward = Constants.String.BLANK
    }
}
