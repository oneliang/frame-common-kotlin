package com.oneliang.ktx.frame.servlet.filter

import com.oneliang.ktx.util.file.FileUtil
import com.oneliang.ktx.util.http.HttpUtil
import com.oneliang.ktx.util.json.JsonArray
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.servlet.*
import javax.servlet.http.HttpServletResponse


class HeaderFilter : Filter {
    companion object {
        private val logger = LoggerManager.getLogger(HeaderFilter::class)
        private const val RESPONSE_HEADER_JSON = "responseHeaderJson"
        private const val HEADER_KEY = "key"
        private const val HEADER_VALUE = "value"
    }

    private val headerList = mutableListOf<HttpUtil.HttpNameValue>()

    /**
     *
     * Method: public void init(FilterConfig filterConfig) throws ServletException
     * @param filterConfig
     * @throws ServletException
     * This method will be initial the key 'encoding' and 'ignore' in web.xml
     */
    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        logger.info("initialize filter:${this::class}")
        val responseHeaderJson = filterConfig.getInitParameter(RESPONSE_HEADER_JSON)
        if (responseHeaderJson != null) {
            val responseHeaderJsonAfterTrim = StringBuilder()
            FileUtil.readInputStreamContentIgnoreLine(ByteArrayInputStream(responseHeaderJson.toByteArray(Charsets.UTF_8)), readFileContentProcessor = object : FileUtil.ReadFileContentProcessor {
                override fun afterReadLine(line: String): Boolean {
                    responseHeaderJsonAfterTrim.append(line.trim())
                    return true
                }
            })
            try {
                val responseHeaderJsonAfterTrimString = responseHeaderJsonAfterTrim.toString()
                logger.info("response header json:$responseHeaderJsonAfterTrimString")
                if (responseHeaderJsonAfterTrimString.isNotBlank()) {
                    val headerJsonArray = JsonArray(responseHeaderJsonAfterTrimString)
                    for (i in 0 until headerJsonArray.length()) {
                        val headerJsonObject = headerJsonArray.getJsonObject(i)
                        headerList.add(HttpUtil.HttpNameValue(headerJsonObject.getString(HEADER_KEY), headerJsonObject.getString(HEADER_VALUE)))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                logger.error("init exception", e)
            }
        }
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
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpServletResponse = servletResponse as HttpServletResponse
        headerList.forEach {
            httpServletResponse.setHeader(it.name, it.value)
        }
        filterChain.doFilter(servletRequest, servletResponse)
    }

    /**
     * Method: public void destory()
     */
    override fun destroy() {
        headerList.clear()
    }
}
