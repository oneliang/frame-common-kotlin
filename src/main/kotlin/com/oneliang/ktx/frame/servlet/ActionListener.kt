package com.oneliang.ktx.frame.servlet

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ConfigurationFactory
import com.oneliang.ktx.frame.servlet.action.*
import com.oneliang.ktx.util.common.KotlinClassUtil
import com.oneliang.ktx.util.common.ObjectUtil
import com.oneliang.ktx.util.common.toObject
import com.oneliang.ktx.util.common.toObjectList
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * com.lwx.frame.servlet.Listener.java
 * @author Dandelion
 * This is only one global servletListener in commonFrame
 * @since 2008-07-31
 */
class ActionListener : HttpServlet() {
    companion object {
        private val logger = LoggerManager.getLogger(ActionListener::class)

        private const val INIT_PARAMETER_CLASS_PROCESSOR = "classProcessor"
    }

    private var classProcessor = KotlinClassUtil.DEFAULT_KOTLIN_CLASS_PROCESSOR

    /**
     * Returns information about the servlet, such as
     * author, version, and copyright.
     *
     * @return String information about this servlet
     */
    override fun getServletInfo(): String {
        return this.javaClass.toString()
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
        //		String uri=request.getRequestURI()
        //		int front=request.getContextPath().length()
        //		uri=uri.substring(front,uri.length())
        //		return 1368624759725l
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
     * The doHead method of the servlet. <br></br>
     *
     * This method is called when a HTTP head request is received.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doHead(request: HttpServletRequest, response: HttpServletResponse) {
        super.doHead(request, response)
        logRequestForOtherCase(request, response, ActionInterface.HttpRequestMethod.HEAD)
    }

    /**
     * The doTrace method of the servlet. <br></br>
     *
     * This method is called when a HTTP trace request is received.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doTrace(request: HttpServletRequest, response: HttpServletResponse) {
        super.doTrace(request, response)
        logRequestForOtherCase(request, response, ActionInterface.HttpRequestMethod.TRACE)
    }

    /**
     * The doOptions method of the servlet. <br></br>
     *
     * This method is called when a HTTP options request is received.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doOptions(request: HttpServletRequest, response: HttpServletResponse) {
        super.doOptions(request, response)
        logRequestForOtherCase(request, response, ActionInterface.HttpRequestMethod.OPTIONS)
    }

    @Throws(ServletException::class, IOException::class)
    private fun logRequestForOtherCase(request: HttpServletRequest, response: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod) {
        val uri = request.requestURI
        logger.info("It is requesting uri:$uri, http method:${httpRequestMethod.name}")
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
        var uri = request.requestURI

        logger.info("It is requesting uri:$uri")

        val front = request.contextPath.length
        //		int rear=uri.lastIndexOf(StaticVar.DOT)
        //		if(rear>front){
        uri = uri.substring(front, uri.length)
        //		}
        //		uri=uri.substring(front,rear)
        logger.info("The request name is:$uri")

        request.setAttribute(ConstantsAction.RequestKey.KEY_STRING_CURRENT_REQUEST_URI, uri)

        //global interceptor doIntercept
        val beforeGlobalInterceptorList = ConfigurationFactory.singletonConfigurationContext.beforeGlobalInterceptorList
        val beforeGlobalInterceptorSign = doGlobalInterceptorList(beforeGlobalInterceptorList, request, response)

        //through the interceptor
        if (!beforeGlobalInterceptorSign) {
            logger.info("The request name:$uri. Can not through the before global interceptors")
            response.sendError(Constants.Http.StatusCode.FORBIDDEN)
            return
        }
        logger.info("Through the before global interceptors!")
        try {
            val actionBeanList = ConfigurationFactory.singletonConfigurationContext.findActionBeanList(uri)
            if (actionBeanList.isNullOrEmpty()) {
                logger.info("The request name:$uri. It does not exist, please config the name and entity class")
                response.sendError(Constants.Http.StatusCode.NOT_FOUND)
                return
            }
            var actionBean: ActionBean? = null
            for (eachActionBean in actionBeanList) {
                if (eachActionBean.isContainHttpRequestMethod(httpRequestMethod)) {
                    actionBean = eachActionBean
                    break
                }
            }
            if (actionBean == null) {
                logger.info("The request name:$uri. Method not allowed, http request method:$httpRequestMethod")
                response.sendError(Constants.Http.StatusCode.METHOD_NOT_ALLOWED)
                return
            }
            //action interceptor doIntercept
            val beforeActionBeanInterceptorList = actionBean.beforeActionInterceptorBeanList
            val beforeActionInterceptorSign = doActionInterceptorBeanList(beforeActionBeanInterceptorList, request, response)
            if (!beforeActionInterceptorSign) {
                logger.info("The request name:$uri. Can not through the before action interceptors")
                response.sendError(Constants.Http.StatusCode.FORBIDDEN)
                return
            }
            logger.info("Through the before action interceptors!")
            val actionInstance = actionBean.actionInstance
            if (actionInstance is ActionInterface) {
                doAction(actionBean, request, response, httpRequestMethod)
            } else {
                doAnnotationAction(actionBean, request, response, httpRequestMethod)
            }
        } catch (e: Throwable) {
            logger.error(Constants.Base.EXCEPTION, e)
            logger.info("The request name:$uri. Action or page does not exist")
            val exceptionPath = ConfigurationFactory.singletonConfigurationContext.globalExceptionForwardPath
            if (exceptionPath != null) {
                logger.info("Forward to exception path:$exceptionPath")
                request.setAttribute(Constants.Base.EXCEPTION, e)
                val requestDispatcher = request.getRequestDispatcher(exceptionPath)
                requestDispatcher.forward(request, response)
            } else {
                logger.info("System can not find the exception path.Please config the global exception forward path.")
                response.sendError(Constants.Http.StatusCode.INTERNAL_SERVER_ERROR)
            }
        }
    }

    /**
     * Initialization of the servlet. <br></br>
     *
     * @throws ServletException if an error occurs
     */
    @Throws(ServletException::class)
    override fun init() {
        logger.info("System is starting up,listener is initial")
        val classProcessorClassName = getInitParameter(INIT_PARAMETER_CLASS_PROCESSOR)
        if (classProcessorClassName != null && classProcessorClassName.isNotBlank()) {
            try {
                val clazz = Thread.currentThread().contextClassLoader.loadClass(classProcessorClassName)
                if (ObjectUtil.isInterfaceImplement(clazz, KotlinClassUtil.KotlinClassProcessor::class.java)) {
                    this.classProcessor = clazz.newInstance() as KotlinClassUtil.KotlinClassProcessor
                }
            } catch (e: Throwable) {
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
    private fun doAction(actionBean: ActionBean, request: HttpServletRequest, response: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod): Boolean {
        val actionInstance = actionBean.actionInstance
        if (actionInstance !is ActionInterface) {
            logger.error("It is not ActionInterface, actionBean:$actionBean, it is impossible")
            return false
        }
        logger.info("Action implements ($actionInstance) is executing")
        //judge is it contain static file page
        val parameterMap = request.parameterMap as Map<String, Array<String>>
        val actionForwardBean = actionBean.findActionForwardBeanByStaticParameter(parameterMap)
        val (normalExecute, needToStaticExecute) = this.getExecuteType(actionForwardBean)
        val forward = if (normalExecute || needToStaticExecute) {
            if (normalExecute) {
                logger.info("Normal executing")
            } else if (needToStaticExecute) {
                logger.info("Need to static execute,first time executing original action")
            }
            actionInstance.execute(request, response)
        } else {
            logger.info("Static execute,not the first time execute")
            actionForwardBean!!.name
        }
        val afterActionBeanInterceptorList = actionBean.afterActionInterceptorBeanList
        val afterActionInterceptorSign = doActionInterceptorBeanList(afterActionBeanInterceptorList, request, response)
        if (!afterActionInterceptorSign) {
            logger.error("Can not through the after action interceptors")
            return false
        }
        logger.info("Through the after action interceptors!")
        val afterGlobalInterceptorList = ConfigurationFactory.singletonConfigurationContext.afterGlobalInterceptorList
        val afterGlobalInterceptorSign = doGlobalInterceptorList(afterGlobalInterceptorList, request, response)
        if (!afterGlobalInterceptorSign) {
            logger.error("Can not through the after global interceptors")
            return false
        }
        logger.info("Through the after global interceptors!")
        if (forward.isNotBlank()) {
            var path = actionBean.findForwardPath(forward)
            if (path.isNotBlank()) {
                logger.info("The forward name in configFile is--:actionPath:" + actionBean.path + "--forward:" + forward + "--path:" + path)
            } else {
                path = ConfigurationFactory.singletonConfigurationContext.findGlobalForwardPath(forward)
                logger.info("The forward name in global forward configFile is--:forward:$forward--path:$path")
            }
            this.doForward(normalExecute, needToStaticExecute, actionForwardBean, path, request, response, false)
        } else {
            logger.info("The forward name--:$forward does not exist,may be ajax use if not please config the name and entity page or class")
        }
        return true
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
    private fun annotationActionMethodParameterValues(annotationActionBean: AnnotationActionBean, request: HttpServletRequest, response: HttpServletResponse): Array<Any?> {
        val annotationActionBeanMethod = annotationActionBean.method!!
        val classes = annotationActionBeanMethod.parameterTypes
        val parameterValues = arrayOfNulls<Any>(classes.size)
        val annotations = annotationActionBeanMethod.parameterAnnotations
        for (i in annotations.indices) {
            if (annotations[i].isNotEmpty() && annotations[i][0] is Action.RequestMapping.RequestParameter) {
                val requestParameterAnnotation = annotations[i][0] as Action.RequestMapping.RequestParameter
                parameterValues[i] = KotlinClassUtil.changeType(classes[i].kotlin, request.getParameterValues(requestParameterAnnotation.value)
                        ?: emptyArray(), Constants.String.BLANK, this.classProcessor)
            } else if (ObjectUtil.isEntity(request, classes[i])) {
                parameterValues[i] = request
            } else if (ObjectUtil.isEntity(response, classes[i])) {
                parameterValues[i] = response
            } else {
                if (KotlinClassUtil.isBaseArray(classes[i].kotlin) || KotlinClassUtil.isSimpleClass(classes[i].kotlin) || KotlinClassUtil.isSimpleArray(classes[i].kotlin)) {
                    parameterValues[i] = KotlinClassUtil.changeType(classes[i].kotlin, emptyArray(), Constants.String.BLANK, this.classProcessor)
                } else if (classes[i].isArray) {
                    val clazz = classes[i].componentType
                    val objectList = request.parameterMap.toObjectList(clazz, this.classProcessor)
                    if (objectList.isNotEmpty()) {
                        val objectArray = objectList.toTypedArray()
                        parameterValues[i] = objectArray
                    }
                } else {
                    val instance = classes[i].newInstance()
                    request.parameterMap.toObject(instance, this.classProcessor)
                    parameterValues[i] = instance
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
    private fun doAnnotationAction(actionBean: ActionBean, request: HttpServletRequest, response: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod): Boolean {
        if (actionBean !is AnnotationActionBean) {
            logger.error("It is not AnnotationActionBean, actionBean:$actionBean, it is impossible")
            return false
        }
        val actionInstance = actionBean.actionInstance
        val parameterMap = request.parameterMap as Map<String, Array<String>>
        val actionForwardBean = actionBean.findActionForwardBeanByStaticParameter(parameterMap)
        val (normalExecute, needToStaticExecute) = this.getExecuteType(actionForwardBean)
        var path: String = Constants.String.BLANK
        if (normalExecute || needToStaticExecute) {
            if (normalExecute) {
                logger.info("Common bean action ($actionInstance) is executing.")
            } else if (needToStaticExecute) {
                logger.info("Need to static execute,first time executing original action")
            }
            val parameterValues = this.annotationActionMethodParameterValues(actionBean, request, response)
            val methodInvokeValue = actionBean.method?.invoke(actionInstance, *parameterValues)
            if (methodInvokeValue != null && methodInvokeValue is String) {
                path = methodInvokeValue.toString()
            } else {
                logger.error("Common bean action $actionInstance is execute error, method is null or method return value is not String")
            }
        } else {
            logger.info("Static execute,not the first time execute")
        }
        val afterActionBeanInterceptorList = actionBean.afterActionInterceptorBeanList
        val afterActionInterceptorSign = doActionInterceptorBeanList(afterActionBeanInterceptorList, request, response)
        if (!afterActionInterceptorSign) {
            logger.error("Can not through the after action interceptors")
            return false
        }
        logger.info("Through the after action interceptors!")
        val afterGlobalInterceptorList = ConfigurationFactory.singletonConfigurationContext.afterGlobalInterceptorList
        val afterGlobalInterceptorSign = doGlobalInterceptorList(afterGlobalInterceptorList, request, response)
        if (!afterGlobalInterceptorSign) {
            logger.error("Can not through the after global interceptors")
            return false
        }
        logger.info("Through the after global interceptors!")
        this.doForward(normalExecute, needToStaticExecute, actionForwardBean, path, request, response, true)
        return true
    }

    /**
     * get execute type
     * @param actionForwardBean
     * @return Pair<Boolean, Boolean>
     */
    private fun getExecuteType(actionForwardBean: ActionForwardBean?): Pair<Boolean, Boolean> {
        var normalExecute = true//default normal execute
        var needToStaticExecute = false
        if (actionForwardBean != null) {//static file page
            normalExecute = false
            val staticFilePathKey = actionForwardBean.staticFilePath
            if (!StaticFilePathUtil.isContainsStaticFilePath(staticFilePathKey)) {
                needToStaticExecute = true
            }
        }
        return normalExecute to needToStaticExecute
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
    private fun doForward(normalExecute: Boolean, needToStaticExecute: Boolean, actionForwardBean: ActionForwardBean?, path: String, request: HttpServletRequest, response: HttpServletResponse, annotationBeanExecute: Boolean) {
        var realPath = path
        if (!normalExecute && !needToStaticExecute) {
            val staticFilePath = actionForwardBean!!.staticFilePath
            logger.info("Send redirect to static file path:$staticFilePath")
            val requestDispatcher = request.getRequestDispatcher(staticFilePath)
            requestDispatcher.forward(request, response)
        } else {
            if (realPath.isNotBlank()) {
                realPath = ActionUtil.parsePath(realPath)
                if (normalExecute) {
                    if (annotationBeanExecute) {
                        logger.info("Annotation bean action executed forward path:$realPath")
                    } else {
                        logger.info("Normal executed forward path:$realPath")
                    }
                    val requestDispatcher = request.getRequestDispatcher(realPath)
                    requestDispatcher.forward(request, response)
                } else if (needToStaticExecute) {
                    val staticFilePath = actionForwardBean!!.staticFilePath
                    val configurationContext = ConfigurationFactory.singletonConfigurationContext
                    if (StaticFilePathUtil.staticize(realPath, configurationContext.projectRealPath + staticFilePath, request, response)) {
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
                    logger.info("System can not find the path:$realPath")
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
    private fun doGlobalInterceptorList(interceptorList: List<InterceptorInterface>?, request: HttpServletRequest, response: HttpServletResponse): Boolean {
        var interceptorSign = true
        if (interceptorList != null) {
            try {
                for (globalInterceptor in interceptorList) {
                    val sign = globalInterceptor.intercept(request, response)
                    logger.info("Global interceptor, through:$sign,interceptor:$globalInterceptor")
                    if (!sign) {
                        interceptorSign = false
                        break
                    }
                }
            } catch (e: Throwable) {
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
                    val actionInterceptor = actionInterceptorBean.interceptorInstance
                    if (actionInterceptor != null) {
                        val sign = actionInterceptor.intercept(request, response)
                        logger.info("Action interceptor, through:$sign,interceptor:$actionInterceptor")
                        if (!sign) {
                            actionInterceptorBeanSign = false
                            break
                        }
                    }
                }
            } catch (e: Throwable) {
                logger.error(Constants.Base.EXCEPTION, e)
                actionInterceptorBeanSign = false
            }

        }
        return actionInterceptorBeanSign
    }
}
