package com.oneliang.ktx.frame.uri

import com.oneliang.ktx.util.logging.LoggerManager
import java.io.IOException
import javax.servlet.*
import javax.servlet.http.HttpServletRequest

class UriMappingFilter : Filter {

    companion object {
        private val logger = LoggerManager.getLogger(UriMappingFilter::class)
    }

    private val uriMappingCache = mutableMapOf<String, String>()

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        logger.info("initialize filter:${this.javaClass.kotlin}")
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpServletRequest = servletRequest as HttpServletRequest
        val requestUri = httpServletRequest.requestURI
        val front = httpServletRequest.contextPath.length
        logger.info("doing filter, request uri:$requestUri")
        val uriFrom = requestUri.substring(front, requestUri.length)
        if (uriMappingCache.containsKey(uriFrom)) {
            val uriTo = uriMappingCache[uriFrom]
            logger.info("uri mapping, find uri in cache, uri from:$uriFrom, uri to:$uriTo")
            servletRequest.getRequestDispatcher(uriTo).forward(servletRequest, servletResponse)
        } else {
            val uriTo = UriMappingContext.findUriTo(uriFrom)
            if (uriTo.isNotBlank()) {
                uriMappingCache[uriFrom] = uriTo
                logger.info("uri mapping, find uri in context, uri from:$uriFrom, uri to:$uriTo")
                servletRequest.getRequestDispatcher(uriTo).forward(servletRequest, servletResponse)
            } else {
                filterChain.doFilter(servletRequest, servletResponse)
            }
        }
    }

    override fun destroy() {
        uriMappingCache.clear()
    }
}