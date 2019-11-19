package com.oneliang.ktx.frame.api

import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.util.json.JsonUtil
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

/**
 * output action map
 */
fun ConfigurationContext.outputApi(outputFilename: String) {
    val apiClassList = AnnotationApiContext.apiClassList
    val apiDocumentObjectMap = AnnotationApiContext.apiDocumentObjectMap
    apiClassList.forEach { apiClass ->
        if (apiClass.java.isAnnotationPresent(Api::class.java)) {
            val api = apiClass.java.getAnnotation(Api::class.java)!!
            val methods = apiClass.java.methods
            for (method in methods) {
                if (method.isAnnotationPresent(Api.Document::class.java)) {
                    val apiDocumentAnnotation = method.getAnnotation(Api.Document::class.java)
                    val apiDocumentKey = apiDocumentAnnotation.key
                    val apiDocumentInputObjectKey = apiDocumentAnnotation.inputObjectKey
                    val apiDocumentOutputObjectKey = apiDocumentAnnotation.outputObjectKey
                    val bufferedWriter = BufferedWriter(FileWriter(File(this.projectRealPath, outputFilename)))
                    bufferedWriter.use {
                        it.write("key:\t$apiDocumentKey")
                        it.newLine()
                        if (apiDocumentInputObjectKey.isNotBlank()) {
                            val inputObject = apiDocumentObjectMap[apiDocumentInputObjectKey]
                            if (inputObject != null) {
                                val inputObjectJson = JsonUtil.objectToJson(inputObject, emptyArray())
                                it.write("input:\t$inputObjectJson")
                                it.newLine()
                            }
                        }
                        if (apiDocumentOutputObjectKey.isNotBlank()) {
                            val outputObject = apiDocumentObjectMap[apiDocumentOutputObjectKey]
                            if (outputObject != null) {
                                val outputObjectJson = JsonUtil.objectToJson(outputObject, emptyArray())
                                it.write("output:\t$outputObjectJson")
                                it.newLine()
                            }
                        }
                        it.flush()
                    }
                }
            }
        }
    }
}