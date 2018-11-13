package com.oneliang.ktx.frame

import java.io.File
import java.util.ArrayList
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

import com.oneliang.Constants
import com.oneliang.exception.FileLoadException
import com.oneliang.ktx.util.common.StringUtil
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.util.common.StringUtil
import com.oneliang.util.jar.JarClassLoader
import com.oneliang.util.jar.JarUtil
import com.oneliang.util.logging.Logger
import com.oneliang.util.logging.LoggerManager
import com.sun.deploy.util.JarUtil

object AnnotationContextUtil {

    private val logger = LoggerManager.getLogger(AnnotationContextUtil::class.java)

    private val SIGN_ROOT = "\$ROOT"
    private val PARAMETER_TYPE = "-T="
    private val PARAMETER_PACKAGE = "-P="
    private val PARAMETER_FILE = "-F="

    /**
     *
     *
     * Method:use for AnnotationActionContext,AnnotationIocContext,AnnotationInterceptorContext,AnnotationMappingContext
     *
     *
     * @param parameters
     * @param classLoader
     * @param classesRealPath
     * @param jarClassLoader
     * @param projectRealPath
     * @param annotationClass
     * @return List<Class></Class>>
     * @throws ClassNotFoundException
     * @throws FileLoadException
     */
    @Throws(ClassNotFoundException::class, FileLoadException::class)
    fun parseAnnotationContextParameter(parameters: String, classLoader: ClassLoader, classesRealPath: String, jarClassLoader: JarClassLoader, projectRealPath: String, annotationClass: Class<out Annotation>): List<Class<*>>? {
        var classList: List<Class<*>>? = null
        val parameterArray = parameters.split(Constants.Symbol.COMMA.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        if (parameterArray != null) {
            if (parameterArray.size == 1) {
                var path = parameters
                var tempClassesRealPath: String? = classesRealPath
                if (tempClassesRealPath == null) {
                    tempClassesRealPath = classLoader.getResource(StringUtil.BLANK)!!.path
                }
                path = tempClassesRealPath!! + path
                classList = searchClassList(tempClassesRealPath, path, annotationClass)
            } else {
                var type: String? = null
                var packageName: String? = null
                var file: String? = null
                for (parameter in parameterArray) {
                    if (parameter.startsWith(PARAMETER_TYPE)) {
                        type = parameter.replaceFirst(PARAMETER_TYPE.toRegex(), StringUtil.BLANK)
                    } else if (parameter.startsWith(PARAMETER_PACKAGE)) {
                        packageName = parameter.replaceFirst(PARAMETER_PACKAGE.toRegex(), StringUtil.BLANK)
                    } else if (parameter.startsWith(PARAMETER_FILE)) {
                        file = parameter.replaceFirst(PARAMETER_FILE.toRegex(), StringUtil.BLANK)
                    }
                }
                if (type != null && type.equals(Constants.File.JAR, ignoreCase = true)) {
                    if (file!!.startsWith(SIGN_ROOT)) {
                        file = file.replace(SIGN_ROOT, projectRealPath)
                    }
                    classList = JarUtil.searchClassList(jarClassLoader, file, packageName, annotationClass)
                }
            }
        }
        return classList
    }

    /**
     * search class list
     * @param classesRealPath
     * @param searchClassPath
     * @param annotationClass
     * @return List<Class></Class>>
     * @throws ClassNotFoundException
     */
    @Throws(ClassNotFoundException::class)
    fun searchClassList(classesRealPath: String?, searchClassPath: String, annotationClass: Class<out Annotation>): List<Class<*>> {
        val classList = ArrayList<Class<*>>()
        val classesRealPathFile = File(classesRealPath!!)
        val classPathFile = File(searchClassPath)
        val queue = ConcurrentLinkedQueue<File>()
        queue.add(classPathFile)
        while (!queue.isEmpty()) {
            val file = queue.poll()
            val filename = file.name
            if (file.isDirectory) {
                val fileList = file.listFiles()
                if (fileList != null) {
                    for (subFile in fileList) {
                        queue.add(subFile)
                    }
                }
            } else if (file.isFile) {
                if (filename.endsWith(Constants.Symbol.DOT + Constants.File.CLASS)) {
                    val filePath = file.absolutePath
                    val className = filePath.substring(classesRealPathFile.absolutePath.length + 1, filePath.length - (Constants.Symbol.DOT + Constants.File.CLASS).length()).replace(File.separator, Constants.Symbol.DOT)
                    val clazz = Thread.currentThread().contextClassLoader.loadClass(className)
                    if (clazz.isAnnotationPresent(annotationClass)) {
                        logger.info(clazz)
                        classList.add(clazz)
                    }
                }
            }
        }
        return classList
    }
}
