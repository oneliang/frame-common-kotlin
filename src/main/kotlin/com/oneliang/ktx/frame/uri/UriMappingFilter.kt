package com.oneliang.ktx.frame.uri

import java.io.IOException
import javax.servlet.*
import javax.servlet.http.HttpServletRequest

class UriMappingFilter : Filter {

    private val uriMappingCache = mutableMapOf<String, String>()

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig?) {
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpServletRequest = servletRequest as HttpServletRequest
        var uri = httpServletRequest.requestURI
        val front = httpServletRequest.contextPath.length
        uri = uri.substring(front, uri.length)
        if (uriMappingCache.containsKey(uri)) {
            val uriTo = uriMappingCache[uri]
            servletRequest.getRequestDispatcher(uriTo).forward(servletRequest, servletResponse)
        } else {
            val uriTo = UriMappingContext.findUriTo(uri)
            if (uriTo.isBlank()) {
                uriMappingCache[uri] = uriTo
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