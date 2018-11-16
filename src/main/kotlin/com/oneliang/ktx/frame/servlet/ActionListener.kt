package com.oneliang.ktx.frame.servlet

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ConfigurationFactory
import com.oneliang.ktx.frame.servlet.action.*
import java.io.IOException
import java.lang.reflect.Array
import java.lang.reflect.InvocationTargetException
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.oneliang.ktx.util.common.KotlinClassUtil
import com.oneliang.ktx.util.common.ObjectUtil
import com.oneliang.ktx.util.common.RequestUtil
import com.oneliang.ktx.util.common.StringUtil
import com.oneliang.ktx.util.logging.LoggerManager

/**
 * com.lwx.frame.servlet.Listener.java
 * @author Dandelion
 * This is only one global servletListener in commonFrame
 * @since 2008-07-31
 */
class ActionListener : HttpServlet() {

    private var classProcessor = KotlinClassUtil.DEFAULT_KOTLIN_CLASS_PROCESSOR

    /**
     * Returns information about the servlet, such as
     * author, version, and copyright.
     *
     * @return String information about this servlet
     */
    override fun getServletInfo(): String {
        this.javaClass.toString()
    }

    @Throws(ServletException::class, IOException::class)
    override fun service(request: HttpServletRequest, response: HttpServletResponse) {
        //servlet bean
        var servletBean: ActionUtil.ServletBean? = ActionUtil.servletBean
        if (servletBean == null) {
            servletBean = ActionUtil.ServletBean()
            ActionUtil.servletBean = servletBean
        }
        servletBean.servletContext = this.servletContext
        servletBean.servletRequest = request
        servletBean.servletResponse = response
        //execute default service method,distribute doGet or doPost or other http method
        super.service(request, response)
        //servlet bean request and response set null
        servletBean.servletRequest = null
        servletBean.servletResponse = null
    }

    override fun getLastModified(request: HttpServletRequest): Long {
        //uri
        //		String uri=request.getRequestURI();
        //		int front=request.getContextPath().length();
        //		uri=uri.substring(front,uri.length());
        //		return 1368624759725l;
        return super.getLastModified(request)
    }

    /**
     * Destruction of the servlet. <br></br>
     */
    override fun destroy() {
        super.destroy() // Just puts "destroy" string in log
        // Put your code here
        logger.info("System is shutting down,listener is deleting,please wait")
    }

    /**
     * The doDelete method of the servlet. <br></br>
     *
     * This method is called when a HTTP delete request is received.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doDelete(request: HttpServletRequest, response: HttpServletResponse) {
        dispatch(request, response, ActionInterface.HttpRequestMethod.DELETE)
    }

    /**
     * The doGet method of the servlet. <br></br>
     *
     * This method is called when a form has its tag value method equals to get.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        dispatch(request, response, ActionInterface.HttpRequestMethod.GET)
    }

    /**
     * The doPost method of the servlet. <br></br>
     *
     * This method is called when a form has its tag value method equals to post.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        dispatch(request, response, ActionInterface.HttpRequestMethod.POST)
    }


    /**
     * The doPut method of the servlet. <br></br>
     *
     * This method is called when a HTTP put request is received.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doPut(request: HttpServletRequest, response: HttpServletResponse) {
        dispatch(request, response, ActionInterface.HttpRequestMethod.PUT)
    }

    /**
     * dispatch http request
     * @param request
     * @param response
     * @param httpRequestMethod
     * @throws ServletException
     * @throws IOException
     */
    @Throws(ServletException::class, IOException::class)
    private fun dispatch(request: HttpServletRequest, response: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod) {
        //uri
        var uri = request.getRequestURI()

        logger.info("System is requesting uri--:$uri")

        val front = request.contextPath.length
        //		int rear=uri.lastIndexOf(StaticVar.DOT);
        //		if(rear>front){
        uri = uri.substring(front, uri.length)
        //		}
        //		uri=uri.substring(front,rear);
        logger.info("The request name is--:$uri")

        //global interceptor doIntercept
        val beforeGlobalInterceptorList = ConfigurationFactory.beforeGlobalInterceptorList
        val beforeGlobalInterceptorSign = doGlobalInterceptorList(beforeGlobalInterceptorList, request, response)

        //through the interceptor
        if (beforeGlobalInterceptorSign) {
            logger.info("Through the before global interceptors!")
            try {
                val actionBeanList = ConfigurationFactory.findActionBeanList(uri)
                if (actionBeanList != null && actionBeanList.isNotEmpty()) {
                    var actionBean: ActionBean? = null
                    for (eachActionBean in actionBeanList) {
                        if (eachActionBean.isContainHttpRequestMethod(httpRequestMethod)) {
                            actionBean = eachActionBean
                            break
                        }
                    }
                    if (actionBean != null) {
                        //action interceptor doIntercept
                        val beforeActionBeanInterceptorList = actionBean.beforeActionInterceptorBeanList
                        val beforeActionInterceptorSign = doActionInterceptorBeanList(beforeActionBeanInterceptorList, request, response)
                        if (beforeActionInterceptorSign) {
                            logger.info("Through the before action interceptors!")
                            val actionInstance = actionBean.actionInstance
                            if (actionInstance is ActionInterface) {
                                doAction(actionBean, request, response, httpRequestMethod)
                            } else {
                                doAnnotationAction(actionBean, request, response, httpRequestMethod)
                            }
                        } else {
                            logger.info("Can not through the before action interceptors")
                            response.sendError(Constants.Http.StatusCode.FORBIDDEN)
                        }
                    } else {
                        logger.info("Method not allowed,http request method:$httpRequestMethod")
                        response.sendError(Constants.Http.StatusCode.METHOD_NOT_ALLOWED)
                    }
                } else {
                    logger.info("The request name--:$uri is not exist,please config the name and entity class")
                    response.sendError(Constants.Http.StatusCode.NOT_FOUND)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                logger.error(Constants.Base.EXCEPTION, e)
                logger.info("Action or page is not exist")
                val exceptionPath = ConfigurationFactory.globalExceptionForwardPath
                if (exceptionPath != null) {
                    request.setAttribute(Constants.Base.EXCEPTION, e)
                    val requestDispatcher = request.getRequestDispatcher(exceptionPath)
                    requestDispatcher.forward(request, response)
                    logger.info("Forward to exception path:$exceptionPath")
                } else {
                    logger.info("System can not find the exception path.Please config the global exception forward path.")
                    response.sendError(Constants.Http.StatusCode.INTERNAL_SERVER_ERROR)
                }
            }

        } else {
            logger.info("Can not through the before global interceptors")
            response.sendError(Constants.Http.StatusCode.FORBIDDEN)
        }
    }

    /**
     * Initialization of the servlet. <br></br>
     *
     * @throws ServletException if an error occurs
     */
    @Throws(ServletException::class)
    fun init() {
        logger.info("System is starting up,listener is initial")
        val classProcessorClassName = getInitParameter(INIT_PARAMETER_CLASS_PROCESSOR)
        if (StringUtil.isNotBlank(classProcessorClassName)) {
            try {
                val clazz = Thread.currentThread().contextClassLoader.loadClass(classProcessorClassName)
                if (ObjectUtil.isInterfaceImplement(clazz, ClassProcessor::class.java)) {
                    this.classProcessor = clazz.newInstance() as ClassProcessor
                }
            } catch (e: Exception) {
                logger.error(Constants.Base.EXCEPTION, e)
            }

        }
    }

    /**
     * do action
     * @param actionBean
     * @param request
     * @param response
     * @return boolean
     * @throws IOException
     * @throws ServletException
     * @throws ActionExecuteException
     */
    @Throws(ActionExecuteException::class, ServletException::class, IOException::class)
    private fun doAction(actionBean: ActionBean, request: HttpServletRequest, response: HttpServletResponse, httpRequestMethod: HttpRequestMethod): Boolean {
        var result = false
        val actionInstance = actionBean.getActionInstance()
        if (actionInstance is ActionInterface) {
            val actionInterface = actionInstance as ActionInterface
            var forward: String? = null
            logger.info("Action implements ($actionInstance) is executing")
            //judge is it contain static file page
            val parameterMap = request.getParameterMap() as Map<String, Array<String>>
            val actionForwardBean = actionBean.findActionForwardBeanByStaticParameter(parameterMap)
            var normalExecute = true//default normal execute
            var needToStaticExecute = false
            if (actionForwardBean != null) {//static file page
                normalExecute = false
                val staticFilePathKey = actionForwardBean!!.getStaticFilePath()
                if (!StaticFilePathUtil.isContainsStaticFilePath(staticFilePathKey)) {
                    needToStaticExecute = true
                }
            }//else normal execute
            if (normalExecute || needToStaticExecute) {
                if (normalExecute) {
                    logger.info("Normal executing")
                } else if (needToStaticExecute) {
                    logger.info("Need to static execute,first time executing original action")
                }
                forward = actionInterface.execute(request, response)
            } else {
                logger.info("Static execute,not the first time execute")
                forward = actionForwardBean!!.getName()
            }
            val afterActionBeanInterceptorList = actionBean.getAfterActionInterceptorBeanList()
            val afterActionInterceptorSign = doActionInterceptorBeanList(afterActionBeanInterceptorList, request, response)
            if (afterActionInterceptorSign) {
                logger.info("Through the after action interceptors!")
                val afterGlobalInterceptorList = ConfigurationFactory.getAfterGlobalInterceptorList()
                val afterGlobalInterceptorSign = doGlobalInterceptorList(afterGlobalInterceptorList, request, response)
                if (afterGlobalInterceptorSign) {
                    logger.info("Through the after global interceptors!")
                    if (forward != null) {
                        var path = actionBean.findForwardPath(forward)
                        if (path != null) {
                            logger.info("The forward name in configFile is--:actionPath:" + actionBean.getPath() + "--forward:" + forward + "--path:" + path)
                        } else {
                            path = ConfigurationFactory.findGlobalForwardPath(forward)
                            logger.info("The forward name in global forward configFile is--:forward:$forward--path:$path")
                        }
                        this.doForward(normalExecute, needToStaticExecute, actionForwardBean, path, request, response, false)
                    } else {
                        logger.info("The forward name--:$forward is not exist,may be ajax use if not please config the name and entity page or class")
                    }
                } else {
                    logger.info("Can not through the after global interceptors")
                }
            } else {
                logger.info("Can not through the after action interceptors")
            }
            result = true
        }
        return result
    }

    /**
     * @param annotationActionBean
     * @param request
     * @param response
     * @return Object[]
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     */
    @Throws(IllegalArgumentException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class)
    private fun annotationActionMethodParameterValues(annotationActionBean: AnnotationActionBean, request: HttpServletRequest, response: HttpServletResponse): Array<Any> {
        val annotationActionBeanMethod = annotationActionBean.getMethod()
        val classes = annotationActionBeanMethod.getParameterTypes()
        val parameterValues = arrayOfNulls<Any>(classes.size)
        val annotations = annotationActionBean.getMethod().getParameterAnnotations()
        for (i in annotations.indices) {
            if (annotations[i].size > 0 && annotations[i][0] is RequestParameter) {
                val requestParameterAnnotation = annotations[i][0] as RequestParameter
                parameterValues[i] = KotlinClassUtil.changeType(classes[i], request.getParameterValues(requestParameterAnnotation.value()), this.classProcessor)
            } else if (ObjectUtil.isEntity(request, classes[i])) {
                parameterValues[i] = request
            } else if (ObjectUtil.isEntity(response, classes[i])) {
                parameterValues[i] = response
            } else {
                if (KotlinClassUtil.isBaseClass(classes[i]) || KotlinClassUtil.isBaseArray(classes[i]) || KotlinClassUtil.isSimpleClass(classes[i]) || KotlinClassUtil.isSimpleArray(classes[i])) {
                    parameterValues[i] = KotlinClassUtil.changeType(classes[i], null, this.classProcessor)
                } else if (classes[i].isArray()) {
                    val clazz = classes[i].getComponentType()
                    val objectList = RequestUtil.requestMapToObjectList(request.getParameterMap(), clazz, this.classProcessor)
                    if (objectList != null && !objectList!!.isEmpty()) {
                        var objectArray = Array.newInstance(clazz, objectList!!.size) as Array<Any>
                        objectArray = objectList!!.toTypedArray()
                        parameterValues[i] = objectArray
                    }
                } else {
                    val `object` = classes[i].newInstance()
                    RequestUtil.requestMapToObject(request.getParameterMap(), `object`, this.classProcessor)
                    parameterValues[i] = `object`
                }
            }
        }
        return parameterValues
    }

    /**
     * do annotation bean
     * @param actionBean
     * @param request
     * @param response
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws ServletException
     */
    @Throws(IllegalArgumentException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class, ServletException::class, IOException::class)
    private fun doAnnotationAction(actionBean: ActionBean, request: HttpServletRequest, response: HttpServletResponse, httpRequestMethod: HttpRequestMethod): Boolean {
        var result = false
        if (actionBean is AnnotationActionBean) {
            val actionInstance = actionBean.getActionInstance()
            val annotationActionBean = actionBean as AnnotationActionBean
            val parameterMap = request.getParameterMap() as Map<String, Array<String>>
            val actionForwardBean = actionBean.findActionForwardBeanByStaticParameter(parameterMap)
            var normalExecute = true//default normal execute
            var needToStaticExecute = false
            var path: String? = null
            if (actionForwardBean != null) {//static file page
                normalExecute = false
                val staticFilePathKey = actionForwardBean!!.getStaticFilePath()
                if (!StaticFilePathUtil.isContainsStaticFilePath(staticFilePathKey)) {
                    needToStaticExecute = true
                }
            }
            if (normalExecute || needToStaticExecute) {
                if (normalExecute) {
                    logger.info("Common bean action ($actionInstance) is executing.")
                } else if (needToStaticExecute) {
                    logger.info("Need to static execute,first time executing original action")
                }
                val parameterValues = this.annotationActionMethodParameterValues(annotationActionBean, request, response)
                val methodInvokeValue = annotationActionBean.getMethod().invoke(actionInstance, parameterValues)
                if (methodInvokeValue != null && methodInvokeValue is String) {
                    path = methodInvokeValue!!.toString()
                }
            } else {
                logger.info("Static execute,not the first time execute")
            }
            val afterActionBeanInterceptorList = actionBean.getAfterActionInterceptorBeanList()
            val afterActionInterceptorSign = doActionInterceptorBeanList(afterActionBeanInterceptorList, request, response)
            if (afterActionInterceptorSign) {
                logger.info("Through the after action interceptors!")
                val afterGlobalInterceptorList = ConfigurationFactory.getAfterGlobalInterceptorList()
                val afterGlobalInterceptorSign = doGlobalInterceptorList(afterGlobalInterceptorList, request, response)
                if (afterGlobalInterceptorSign) {
                    logger.info("Through the after global interceptors!")
                    this.doForward(normalExecute, needToStaticExecute, actionForwardBean, path, request, response, true)
                }
            }
            result = true
        }
        return result
    }

    /**
     * do forward
     * @param normalExecute
     * @param needToStaticExecute
     * @param actionForwardBean
     * @param path
     * @param request
     * @param response
     * @param annotationBeanExecute
     * @throws IOException
     * @throws ServletException
     */
    @Throws(ServletException::class, IOException::class)
    private fun doForward(normalExecute: Boolean, needToStaticExecute: Boolean, actionForwardBean: ActionForwardBean?, path: String?, request: HttpServletRequest, response: HttpServletResponse, annotationBeanExecute: Boolean) {
        var path = path
        if (!normalExecute && !needToStaticExecute) {
            val staticFilePath = actionForwardBean!!.getStaticFilePath()
            logger.info("Send redirect to static file path:$staticFilePath")
            val requestDispatcher = request.getRequestDispatcher(staticFilePath)
            requestDispatcher.forward(request, response)
        } else {
            if (StringUtil.isNotBlank(path)) {
                path = ActionUtil.parsePath(path)
                if (normalExecute) {
                    if (annotationBeanExecute) {
                        logger.info("Annotation bean action executed forward path:" + path!!)
                    } else {
                        logger.info("Normal executed forward path:" + path!!)
                    }
                    val requestDispatcher = request.getRequestDispatcher(path)
                    requestDispatcher.forward(request, response)
                } else if (needToStaticExecute) {
                    val staticFilePath = actionForwardBean!!.getStaticFilePath()
                    val configurationContext = ConfigurationFactory.getSingletonConfigurationContext()
                    if (StaticFilePathUtil.staticize(path, configurationContext.getProjectRealPath() + staticFilePath, request, response)) {
                        logger.info("Static executed success,redirect static file:$staticFilePath")
                        val requestDispatcher = request.getRequestDispatcher(staticFilePath)
                        requestDispatcher.forward(request, response)
                        StaticFilePathUtil.addStaticFilePath(staticFilePath, staticFilePath)
                    } else {
                        logger.info("Static executed failure,file:$staticFilePath")
                    }
                }
            } else {
                if (annotationBeanExecute) {
                    logger.info("May be ajax use if not please config the entity page with String type.")
                } else {
                    logger.info("System can not find the path:" + path!!)
                }
            }
        }
    }

    /**
     * do global interceptor list,include global(before,after)
     * @param interceptorList
     * @param request
     * @param response
     * @return boolean
     */
    private fun doGlobalInterceptorList(interceptorList: List<Interceptor>?, request: HttpServletRequest, response: HttpServletResponse): Boolean {
        var interceptorSign = true
        if (interceptorList != null) {
            try {
                for (globalInterceptor in interceptorList) {
                    val sign = globalInterceptor.doIntercept(request, response)
                    logger.info("Global intercept:$sign,interceptor:$globalInterceptor")
                    if (!sign) {
                        interceptorSign = false
                        break
                    }
                }
            } catch (e: Exception) {
                logger.error(Constants.Base.EXCEPTION, e)
                interceptorSign = false
            }

        }
        return interceptorSign
    }

    /**
     * do action bean interceptor list,include action(before,action)
     * @param actionInterceptorBeanList
     * @param request
     * @param response
     * @return boolean
     */
    private fun doActionInterceptorBeanList(actionInterceptorBeanList: List<ActionInterceptorBean>?, request: HttpServletRequest, response: HttpServletResponse): Boolean {
        var actionInterceptorBeanSign = true
        if (actionInterceptorBeanList != null) {
            try {
                for (actionInterceptorBean in actionInterceptorBeanList) {
                    val actionInterceptor = actionInterceptorBean.getInterceptorInstance()
                    if (actionInterceptor != null) {
                        val sign = actionInterceptor!!.doIntercept(request, response)
                        logger.info("Action intercept:$sign,interceptor:$actionInterceptor")
                        if (!sign) {
                            actionInterceptorBeanSign = false
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error(Constants.Base.EXCEPTION, e)
                actionInterceptorBeanSign = false
            }

        }
        return actionInterceptorBeanSign
    }

    companion object {

        /**
         *
         * Property: serialVersionUID
         * serialVersionUID
         */
        private val serialVersionUID = 8982018678465106212L

        private val logger = LoggerManager.getLogger(ActionListener::class.java)

        private val INIT_PARAMETER_CLASS_PROCESSOR = "classProcessor"
    }
}
