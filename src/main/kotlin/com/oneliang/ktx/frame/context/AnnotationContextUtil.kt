package com.oneliang.ktx.frame.context

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.FileLoadException
import com.oneliang.ktx.util.jar.JarClassLoader
import com.oneliang.ktx.util.jar.JarUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KClass

object AnnotationContextUtil {

    private val logger = LoggerManager.getLogger(AnnotationContextUtil::class)

    private const val SIGN_ROOT = "\$ROOT"
    private const val PARAMETER_TYPE = "-T="
    private const val PARAMETER_PACKAGE = "-P="
    private const val PARAMETER_FILE = "-F="

    private object Type {
        const val JAR = Constants.File.JAR
        const val CLASS_DIRECTORY = "class_directory"
    }

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
    fun parseAnnotationContextParameterAndSearchClass(parameters: String, classLoader: ClassLoader, classesRealPath: String, jarClassLoader: JarClassLoader, projectRealPath: String, annotationClass: KClass<out Annotation>): List<KClass<*>> {
        var classList: List<KClass<*>> = emptyList()
        val parameterArray = parameters.split(Constants.Symbol.COMMA)
        if (parameterArray.size == 1) {
            var path = parameters
            val tempClassesRealPath = if (classesRealPath.isBlank()) {
                classLoader.getResource(Constants.String.BLANK).path
            } else {
                classesRealPath
            }
            path = File(tempClassesRealPath, path).absolutePath
            logger.debug("search class path:$path")
            classList = searchClassList(tempClassesRealPath, path, annotationClass)
        } else {
            var type: String = Constants.String.BLANK
            var packageName: String = Constants.String.BLANK
            var file: String = Constants.String.BLANK
            for (parameter in parameterArray) {
                when {
                    parameter.startsWith(PARAMETER_TYPE) -> type = parameter.replaceFirst(PARAMETER_TYPE, Constants.String.BLANK)
                    parameter.startsWith(PARAMETER_PACKAGE) -> packageName = parameter.replaceFirst(PARAMETER_PACKAGE, Constants.String.BLANK)
                    parameter.startsWith(PARAMETER_FILE) -> file = parameter.replaceFirst(PARAMETER_FILE, Constants.String.BLANK)
                }
            }
            if (type.equals(Type.JAR, ignoreCase = true)) {
                if (file.startsWith(SIGN_ROOT)) {
                    file = file.replace(SIGN_ROOT, projectRealPath)
                }
                file = File(file).absolutePath
                logger.debug("search class file:$file")
                classList = JarUtil.searchClassList(jarClassLoader, file, packageName, annotationClass)
            } else if (type.equals(Type.CLASS_DIRECTORY, ignoreCase = true)) {
                val directoryList = file.split(Constants.Symbol.SEMICOLON)
                val searchClassList = mutableListOf<KClass<*>>()
                val mainClassesRealPath = if (classesRealPath.isBlank()) {
                    classLoader.getResource(Constants.String.BLANK).path
                } else {
                    classesRealPath
                }
                for (directory in directoryList) {
                    val otherClassesRealPath = File(mainClassesRealPath, directory).absolutePath
                    searchClassList.addAll(searchClassList(otherClassesRealPath, "", annotationClass))
                }
                classList = searchClassList
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
    fun searchClassList(classesRealPath: String, searchClassPath: String, annotationClass: KClass<out Annotation>): List<KClass<*>> {
        val classList = mutableListOf<KClass<*>>()
        val classesRealPathFile = File(classesRealPath)
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
                    val className = filePath.substring(classesRealPathFile.absolutePath.length + 1, filePath.length - (Constants.Symbol.DOT + Constants.File.CLASS).length).replace(File.separator, Constants.Symbol.DOT)
                    val clazz = Thread.currentThread().contextClassLoader.loadClass(className)
                    if (clazz.isAnnotationPresent(annotationClass.java)) {
                        classList.add(clazz.kotlin)
                    }
                }
            }
        }
        return classList
    }
}
