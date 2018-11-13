package com.oneliang.ktx.frame.servlet.action

import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

import com.oneliang.frame.bean.Page
import com.oneliang.frame.servlet.ActionUtil
import com.oneliang.ktx.frame.servlet.ActionUtil
import com.oneliang.util.common.ClassUtil
import com.oneliang.util.common.RequestUtil
import com.oneliang.util.common.ClassUtil.ClassProcessor

/**
 *
 *
 * Class: abstract class,make sub class union
 *
 *
 * com.lwx.frame.servlet.CommonAction
 * abstract class
 * @author Dandelion
 * @since 2008-07-31
 */
abstract class CommonAction : ActionInterface {

    protected var classProcessor = ClassUtil.DEFAULT_CLASS_PROCESSOR

    /**
     *
     * Method: get page
     * @return Page
     */
    protected val page: Page
        get() {
            val request = ActionUtil.getServletRequest()
            return getPage(request)
        }

    /**
     *
     *
     * Method: set the instance object to the request
     *
     * @param <T>
     * @param key
     * @param value
    </T> */
    protected fun <T : Any> setObjectToRequest(key: String, value: T) {
        val request = ActionUtil.getServletRequest()
        this.setObjectToRequest(request, key, value)
    }

    /**
     *
     *
     * Method: set the instance object to the request
     *
     *
     * @param request
     */
    protected fun <T : Any> setObjectToRequest(request: ServletRequest, key: String, value: T) {
        request.setAttribute(key, value)
    }

    /**
     *
     *
     * Method: set the request values to object
     *
     * @param <T>
     * @param object
    </T> */
    protected fun <T : Any> requestValuesToObject(`object`: T) {
        val request = ActionUtil.getServletRequest()
        this.requestValuesToObject(request, `object`)
    }

    /**
     *
     *
     * Method: set the request values to object
     *
     *
     * @param <T>
     * @param request
     * @param object
    </T> */
    protected fun <T : Any> requestValuesToObject(request: ServletRequest, `object`: T) {
        val map = request.getParameterMap()
        RequestUtil.requestMapToObject(map, `object`, classProcessor)
    }

    /**
     *
     *
     * Method: set the instance object to the session
     *
     * @param <T>
     * @param key
     * @param value
    </T> */
    protected fun <T : Any> setObjectToSession(key: String, value: T) {
        val request = ActionUtil.getServletRequest()
        this.setObjectToSession(request, key, value)
    }

    /**
     *
     *
     * Method: set the instance object to the session
     *
     *
     * @param <T>
     * @param request
     * @param key
     * @param value
    </T> */
    protected fun <T : Any> setObjectToSession(request: ServletRequest, key: String,
                                               value: T) {
        (request as HttpServletRequest).getSession().setAttribute(key, value)
    }

    /**
     *
     *
     * Method: get the instance object to the session by key
     *
     * @param key
     * @return Object
     */
    protected fun getObjectFromSession(key: String): Any {
        val request = ActionUtil.getServletRequest()
        return this.getObjectFromSession(request, key)
    }

    /**
     *
     *
     * Method: get the instance object to the session by key
     *
     *
     * @param request
     * @param key
     * @return Object
     */
    protected fun getObjectFromSession(request: ServletRequest, key: String): Any {
        return (request as HttpServletRequest).getSession().getAttribute(key)
    }

    /**
     *
     * Method: remove object from session
     * @param key
     */
    protected fun removeObjectFromSession(key: String) {
        val request = ActionUtil.getServletRequest()
        removeObjectFromSession(request, key)
    }

    /**
     *
     * Method: remove object from session
     * @param request
     * @param key
     */
    protected fun removeObjectFromSession(request: ServletRequest, key: String) {
        (request as HttpServletRequest).getSession().removeAttribute(key)
    }

    /**
     *
     *
     * Method: get the parameter from request
     *
     * @param parameter
     * @return String
     */
    protected fun getParameter(parameter: String): String {
        val request = ActionUtil.getServletRequest()
        return this.getParameter(request, parameter)
    }

    /**
     *
     *
     * Method: get the parameter from request
     *
     *
     * @param request
     * @param parameter
     * @return String
     */
    protected fun getParameter(request: ServletRequest, parameter: String): String {
        return request.getParameter(parameter)
    }

    /**
     *
     *
     * Method:get the parameter values from request
     *
     * @param parameter
     * @return String[]
     */
    protected fun getParameterValues(parameter: String): Array<String> {
        val request = ActionUtil.getServletRequest()
        return this.getParameterValues(request, parameter)
    }

    /**
     *
     *
     * Method:get the parameter values from request
     *
     * @param request
     * @param parameter
     * @return String[]
     */
    protected fun getParameterValues(request: ServletRequest, parameter: String): Array<String> {
        return request.getParameterValues(parameter)
    }

    /**
     * forward
     * @param path
     * @throws ActionExecuteException
     */
    @Throws(ActionExecuteException::class)
    protected fun forward(path: String) {
        val request = ActionUtil.getServletRequest()
        val response = ActionUtil.getServletResponse()
        try {
            this.forward(request, response, path)
        } catch (e: Exception) {
            throw ActionExecuteException(e)
        }

    }

    /**
     * request.getRequestDispatcher(path).forward(request,response);
     * @param request
     * @param response
     * @param path
     * @throws ActionExecuteException
     */
    @Throws(ActionExecuteException::class)
    protected fun forward(request: ServletRequest, response: ServletResponse, path: String) {
        try {
            request.getRequestDispatcher(path).forward(request, response)
        } catch (e: Exception) {
            throw ActionExecuteException(e)
        }

    }

    /**
     * write
     * @param string
     * @throws ActionExecuteException
     */
    @Throws(ActionExecuteException::class)
    protected fun write(string: String) {
        val response = ActionUtil.getServletResponse()
        try {
            this.write(response, string)
        } catch (e: Exception) {
            throw ActionExecuteException(e)
        }

    }

    /**
     * write
     * @param response
     * @param string
     * @throws ActionExecuteException
     */
    @Throws(ActionExecuteException::class)
    protected fun write(response: ServletResponse, string: String) {
        try {
            response.getWriter().write(string)
        } catch (e: Exception) {
            throw ActionExecuteException(e)
        }

    }

    /**
     *
     * Method: get page
     * @param request
     * @return Page
     */
    protected fun getPage(request: ServletRequest): Page {
        val page = Page()
        this.requestValuesToObject<Any>(request, page)
        return page
    }

    /**
     * @param classProcessor the classProcessor to set
     */
    fun setClassProcessor(classProcessor: ClassProcessor) {
        this.classProcessor = classProcessor
    }
}
