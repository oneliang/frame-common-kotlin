package com.oneliang.ktx.frame.api

import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.servlet.action.Action
import com.oneliang.ktx.frame.servlet.action.ActionContext
import com.oneliang.ktx.frame.servlet.action.AnnotationActionBean
import com.oneliang.ktx.util.json.JsonUtil
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import kotlin.reflect.KClass

/**
 * output action map
 */
fun ConfigurationContext.outputActionAndApi(outputFilename: String) {
    val apiClassList = AnnotationApiContext.apiClassList
    val apiClassListMap = mutableMapOf<String, MutableList<Pair<Api, KClass<*>>>>()
    apiClassList.forEach {
        if (it.java.isAnnotationPresent(Api::class.java)) {
            val api = it.java.getAnnotation(Api::class.java)!!
            val uri = api.requestMapping
            val uriApiClassList = if (apiClassListMap.containsKey(uri)) {
                apiClassListMap[uri]!!
            } else {
                val list = mutableListOf<Pair<Api, KClass<*>>>()
                apiClassListMap[uri] = list
                list
            }
            uriApiClassList += api to it
        } else {
            error("$it is not api class, it is impossible.")
        }
    }
    val sortedActionBeanMap = ActionContext.actionBeanMap.toSortedMap()
    val bufferedWriter = BufferedWriter(FileWriter(File(this.projectRealPath, outputFilename)))
    bufferedWriter.use {
        sortedActionBeanMap.forEach { (_, actionBean) ->
            it.newLine()
            val uri = actionBean.path
            it.write("uri:$uri")
            it.newLine()
            it.write("\tmethods:${actionBean.httpRequestMethods}")
            it.newLine()
            if (actionBean is AnnotationActionBean) {
                val annotationActionBeanMethod = actionBean.method!!
                val classes = annotationActionBeanMethod.parameterTypes
                val parameterAnnotations = annotationActionBeanMethod.parameterAnnotations
                parameterAnnotations.forEachIndexed { index, annotationArray ->
                    if (annotationArray.isNotEmpty() && annotationArray[0] is Action.RequestMapping.RequestParameter) {
                        val parameterAnnotation = annotationArray[0] as Action.RequestMapping.RequestParameter
                        it.write("\tparameter$index(${parameterAnnotation.value}):${classes[index].name}")
                        it.newLine()
                    }
                }
            }
            if (apiClassListMap.containsKey(uri)) {
                val uriApiClassList = apiClassListMap[uri] ?: error("uri:$uri is not exists, it is impossible.")
                uriApiClassList.forEach { (api, apiClass) ->
                    val instance = apiClass.java.newInstance()
                    val apiJson = JsonUtil.objectToJson(instance, emptyArray())
                    if (api.mode == Api.Mode.REQUEST) {
                        it.write("api request json:$apiJson")
                    } else {
                        it.write("api response json:$apiJson")
                    }
                }
            }
            it.newLine()
            it.flush()
        }
    }
}