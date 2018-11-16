package com.oneliang.ktx.frame.ioc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.AbstractContext
import com.oneliang.ktx.frame.ioc.aop.*
import com.oneliang.ktx.util.common.JavaXmlUtil
import com.oneliang.ktx.util.common.KotlinClassUtil
import com.oneliang.ktx.util.common.ObjectUtil
import com.oneliang.ktx.util.common.ProxyUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.lang.reflect.Constructor

/**
 * so far,only this context use proxy
 * @author Dandelion
 */
open class IocContext : AbstractContext() {
    companion object {
        private val logger = LoggerManager.getLogger(IocContext::class)
        internal val iocConfigurationBean = IocConfigurationBean()
        internal val iocBeanMap = mutableMapOf<String, IocBean>()
    }

    /**
     * initialize
     * @param parameters
     */
    override fun initialize(parameters: String) {
        try {
            var path = parameters
            val tempClassesRealPath = if (classesRealPath.isBlank()) {
                this.classLoader.getResource(Constants.String.BLANK).path
            } else {
                classesRealPath
            }
            path = tempClassesRealPath!! + path
            val document = JavaXmlUtil.parse(path)
            val root = document.documentElement
            //configuration
            val configurationElementList = root.getElementsByTagName(IocConfigurationBean.TAG_CONFIGURATION)
            if (configurationElementList != null && configurationElementList.length > 0) {
                val configurationAttributeMap = configurationElementList.item(0).attributes
                JavaXmlUtil.initializeFromAttributeMap(iocConfigurationBean, configurationAttributeMap)
            }
            //ioc bean
            val beanElementList = root.getElementsByTagName(IocBean.TAG_BEAN)
            //xml to object
            if (beanElementList != null) {
                val beanElementLength = beanElementList.length
                for (index in 0 until beanElementLength) {
                    val beanElement = beanElementList.item(index)
                    //bean
                    val iocBean = IocBean()
                    val attributeMap = beanElement.attributes
                    JavaXmlUtil.initializeFromAttributeMap(iocBean, attributeMap)
                    //constructor
                    val childNodeList = beanElement.childNodes
                    if (childNodeList != null) {
                        val childNodeLength = childNodeList.length
                        for (childNodeIndex in 0 until childNodeLength) {
                            val childNode = childNodeList.item(childNodeIndex)
                            val nodeName = childNode.nodeName
                            if (nodeName == IocConstructorBean.TAG_CONSTRUCTOR) {
                                val iocConstructorBean = IocConstructorBean()
                                val iocConstructorAttributeMap = childNode.attributes
                                JavaXmlUtil.initializeFromAttributeMap(iocConstructorBean, iocConstructorAttributeMap)
                                iocBean.iocConstructorBean = iocConstructorBean
                            } else if (nodeName == IocPropertyBean.TAG_PROPERTY) {
                                val iocPropertyBean = IocPropertyBean()
                                val iocPropertyAttributeMap = childNode.attributes
                                JavaXmlUtil.initializeFromAttributeMap(iocPropertyBean, iocPropertyAttributeMap)
                                iocBean.addIocPropertyBean(iocPropertyBean)
                            } else if (nodeName == IocAfterInjectBean.TAG_AFTER_INJECT) {
                                val iocAfterInjectBean = IocAfterInjectBean()
                                val iocAfterInjectAttributeMap = childNode.attributes
                                JavaXmlUtil.initializeFromAttributeMap(iocAfterInjectBean, iocAfterInjectAttributeMap)
                                iocBean.addIocAfterInjectBean(iocAfterInjectBean)
                            }//after inject
                            //property
                        }
                    }
                    if (!iocBeanMap.containsKey(iocBean.id)) {
                        iocBeanMap[iocBean.id] = iocBean
                    }
                }
            }
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }

    }

    /**
     * destroy
     */
    override fun destroy() {
        iocBeanMap.clear()
    }

    /**
     * ioc bean object instantiated
     * @throws Exception
     */
    @Throws(Exception::class)
    protected fun iocBeanObjectInstantiated() {
        val iterator = iocBeanMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val iocBean = entry.value
            if (iocBean.iocConstructorBean != null) {
                iocBeanObjectInstantiatedByConstructor(iocBean)
            } else {
                iocBeanObjectInstantiatedByDefaultConstructor(iocBean)
            }
        }
    }

    /**
     * instantiated one ioc bean by constructor
     * @param iocBean
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun iocBeanObjectInstantiatedByConstructor(iocBean: IocBean) {
        val iocConstructorBean = iocBean.iocConstructorBean
        val type = iocBean.type
        val beanClass = iocBean.beanClass
        val constructorTypes = iocConstructorBean!!.types
        val constructorReferences = iocConstructorBean.references
        val constructorTypeArray = constructorTypes.split(Constants.Symbol.COMMA)
        val constructorReferenceArray = constructorReferences.split(Constants.Symbol.COMMA)
        val constructorTypeClassArray = arrayOfNulls<Class<*>>(constructorTypeArray.size)
        val constructorReferenceObjectArray = arrayOfNulls<Any>(constructorReferenceArray.size)
        var index = 0
        for (constructorType in constructorTypeArray) {
            constructorTypeClassArray[index++] = KotlinClassUtil.getClass(this.classLoader, constructorType)?.java
        }
        index = 0
        for (constructorReference in constructorReferenceArray) {
            val referenceObject = iocBeanMap[constructorReference]
            if (referenceObject != null) {
                var referenceProxyObject = referenceObject.proxyInstance
                if (referenceProxyObject == null) {
                    iocBeanObjectInstantiatedByDefaultConstructor(referenceObject)
                    referenceProxyObject = referenceObject.proxyInstance
                }
                constructorReferenceObjectArray[index++] = referenceProxyObject
            } else {
                constructorReferenceObjectArray[index++] = null
            }
        }
        val constructor: Constructor<*> = if (beanClass == null) {
            this.classLoader.loadClass(type).getConstructor(*constructorTypeClassArray)
        } else {
            beanClass.getConstructor(*constructorTypeClassArray)
        }
        val beanInstance = constructor.newInstance(*constructorReferenceObjectArray)
        iocBean.beanInstance = beanInstance
        if (iocBean.proxy) {
            val classLoader = if (beanClass == null) {
                this.classLoader
            } else {
                beanClass.classLoader
            }
            val proxyInstance = ProxyUtil.newProxyInstance(classLoader, beanInstance, AopInvocationHandler<Any>(beanInstance))
            iocBean.proxyInstance = proxyInstance
        } else {
            iocBean.proxyInstance = beanInstance
        }
    }

    /**
     * instantiated one ioc bean by default constructor
     * @param iocBean
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun iocBeanObjectInstantiatedByDefaultConstructor(iocBean: IocBean) {
        val type = iocBean.type
        val beanClass = iocBean.beanClass
        val value = iocBean.value
        var beanInstance = iocBean.beanInstance
        if (beanInstance == null) {
            val iocBeanId = iocBean.id
            if (objectMap.containsKey(iocBeanId)) {//object map contain,prove duplicate config in ioc,copy to ioc bean
                beanInstance = objectMap.get(iocBeanId)
                iocBean.beanInstance = beanInstance
                iocBean.proxyInstance = beanInstance
            } else {//normal config
                if (KotlinClassUtil.isBaseClass(type) || KotlinClassUtil.isSimpleClass(type)) {
                    val clazz = KotlinClassUtil.getClass(this.classLoader, type)!!
                    beanInstance = KotlinClassUtil.changeType(clazz, arrayOf(value))
                } else {
                    beanInstance = if (beanClass == null) {
                        this.classLoader.loadClass(type).newInstance()
                    } else {
                        beanClass.newInstance()
                    }
                    //aop interceptor
                    if (beanInstance is BeforeInvokeProcessor) {
                        AopInvocationHandler.addBeforeInvokeProcessor(beanInstance)
                    }
                    if (beanInstance is AfterReturningProcessor) {
                        AopInvocationHandler.addAfterReturningProcessor(beanInstance)
                    }
                    if (beanInstance is AfterThrowingProcessor) {
                        AopInvocationHandler.addAfterThrowingProcessor(beanInstance)
                    }
                    if (beanInstance is InvokeProcessor) {
                        AopInvocationHandler.setInvokeProcessor(beanInstance)
                    }
                }
                iocBean.beanInstance = beanInstance!!
                if (iocBean.proxy) {
                    val classLoader = if (beanClass == null) {
                        this.classLoader
                    } else {
                        beanClass.classLoader
                    }
                    val proxyInstance = ProxyUtil.newProxyInstance(classLoader, beanInstance, AopInvocationHandler<Any>(beanInstance))
                    iocBean.proxyInstance = proxyInstance
                } else {
                    iocBean.proxyInstance = beanInstance
                }
            }
            logger.info(iocBean.type + "<->id:" + iocBeanId + "<->proxy:" + iocBean.proxyInstance + "<->instance:" + iocBean.beanInstance)
        }
    }

    /**
     * inject,only this injection put the proxy instance to object map
     * @throws Exception
     */
    @Throws(Exception::class)
    fun inject() {
        //instantiated all ioc bean
        iocBeanObjectInstantiated()
        //inject
        val objectInjectType = iocConfigurationBean.objectInjectType
        if (objectInjectType == IocConfigurationBean.INJECT_TYPE_AUTO_BY_ID) {
            objectMap.forEach { (_, instance) ->
                this.autoInjectObjectById(instance)
            }
        } else if (objectInjectType == IocConfigurationBean.INJECT_TYPE_AUTO_BY_TYPE) {
            objectMap.forEach { (_, instance) ->
                this.autoInjectObjectByType(instance)
            }
        }
        iocBeanMap.forEach { (_, iocBean) ->
            val injectType = iocBean.injectType
            val iocBeanId = iocBean.id
            val beanInstance = iocBean.beanInstance!!
            when (injectType) {
                IocBean.INJECT_TYPE_AUTO_BY_ID -> this.autoInjectObjectById(beanInstance)
                IocBean.INJECT_TYPE_AUTO_BY_TYPE -> this.autoInjectObjectByType(beanInstance)
                IocBean.INJECT_TYPE_MANUAL -> this.manualInject(iocBean)
            }
            if (!objectMap.containsKey(iocBeanId)) {
                objectMap[iocBeanId] = iocBean.proxyInstance!!
            }
        }
    }

    /**
     * auto inject instance by type
     * @throws Exception
     */
    @Throws(Exception::class)
    protected fun autoInjectObjectByType(instance: Any) {
        val objectMethods = instance.javaClass.methods
        for (method in objectMethods) {
            val methodName = method.name
            if (methodName.startsWith(Constants.Method.PREFIX_SET)) {
                val types = method.parameterTypes
                if (types != null && types.size == 1) {
                    val parameterClass = types[0]
                    val parameterClassName = parameterClass.name
                    iocBeanMap.forEach { (_, iocBean) ->
                        val beanInstance = iocBean.beanInstance
                        val proxyInstance = iocBean.proxyInstance
                        val beanInstanceClassName = beanInstance!!.javaClass.name
                        if (parameterClassName == beanInstanceClassName) {
                            logger.info(instance.javaClass.name + "<-" + beanInstance.javaClass.name)
                            method.invoke(instance, proxyInstance)
                        } else {
                            val interfaces = beanInstance.javaClass.interfaces
                            if (interfaces != null) {
                                for (interfaceClass in interfaces) {
                                    val beanInstanceClassInterfaceName = interfaceClass.name
                                    if (parameterClassName == beanInstanceClassInterfaceName) {
                                        logger.info(instance.javaClass.name + "<-" + beanInstance.javaClass.name)
                                        method.invoke(instance, proxyInstance)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * auto inject object by id
     * @throws Exception
     */
    @Throws(Exception::class)
    fun autoInjectObjectById(instance: Any) {
        val objectMethods = instance.javaClass.methods
        for (method in objectMethods) {
            val methodName = method.name
            if (methodName.startsWith(Constants.Method.PREFIX_SET)) {
                val types = method.parameterTypes
                if (types != null && types.size == 1) {
                    val fieldName = ObjectUtil.methodNameToFieldName(Constants.Method.PREFIX_SET, methodName)
                    val iocBean = iocBeanMap[fieldName]
                    if (iocBean != null) {
                        val proxyInstance = iocBean.proxyInstance
                        logger.info(instance.javaClass.name + "<-" + iocBean.type)
                        method.invoke(instance, proxyInstance)
                    }
                }
            }
        }
    }

    /**
     * manual inject,must config all bean in ioc
     * @throws Exception
     */
    @Throws(Exception::class)
    protected fun manualInject(iocBean: IocBean) {
        val id = iocBean.id
        val iocPropertyBeanList = iocBean.iocPropertyBeanList
        for (iocPropertyBean in iocPropertyBeanList) {
            val propertyName = iocPropertyBean.name
            val referenceBeanId = iocPropertyBean.reference
            if (iocBeanMap.containsKey(referenceBeanId)) {
                val beanInstance = iocBean.beanInstance
                val objectMethods = beanInstance!!.javaClass.methods
                for (method in objectMethods) {
                    val methodName = method.name
                    if (methodName.startsWith(Constants.Method.PREFIX_SET)) {
                        val fieldName = ObjectUtil.methodNameToFieldName(Constants.Method.PREFIX_SET, methodName)
                        if (propertyName == fieldName) {
                            val types = method.parameterTypes
                            if (types != null && types.size == 1) {
                                val referenceObject = iocBeanMap[referenceBeanId]
                                if (referenceObject != null) {
                                    val proxyInstance = referenceObject.proxyInstance
                                    logger.info(iocBean.type + "<-" + referenceObject.type)
                                    method.invoke(beanInstance, proxyInstance)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!objectMap.containsKey(id)) {
            objectMap[id] = iocBean.proxyInstance!!
        }
    }

    /**
     * after inject
     * @throws Exception
     */
    @Throws(Exception::class)
    fun afterInject() {
        iocBeanMap.forEach { (_, iocBean) ->
            val iocAfterInjectBeanList = iocBean.iocAfterInjectBeanList
            for (iocAfterInjectBean in iocAfterInjectBeanList) {
                val instance = iocBean.proxyInstance!!
                val method = instance.javaClass.getMethod(iocAfterInjectBean.method)
                method.invoke(instance)
            }
        }
    }

    /**
     * put to ioc bean map
     * @param key
     * @param iocBean
     */
    fun putToIocBeanMap(key: String, iocBean: IocBean) {
        iocBeanMap[key] = iocBean
    }
}
