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
                    val bufferedWriter = BufferedWriter(FileWriter(File(this.projectRealPath, outputFilename)))
                    bufferedWriter.use {
                        it.write("key:\t$apiDocumentKey")
                        it.newLine()
                        val caseArray = apiDocumentAnnotation.cases
                        caseArray.forEach { case ->
                            val caseKey = case.key
                            val caseInputObjectKey = case.inputObjectKey
                            val caseOutputObjectKey = case.outputObjectKey
                            it.write("\tcase key:\t$apiDocumentKey")
                            it.newLine()
                            if (caseInputObjectKey.isNotBlank()) {
                                val inputObject = apiDocumentObjectMap[caseInputObjectKey]
                                if (inputObject != null) {
                                    val inputObjectJson = JsonUtil.objectToJson(inputObject, emptyArray())
                                    it.write("\t\tcase input:\t$inputObjectJson")
                                    it.newLine()
                                }
                            }
                            if (caseOutputObjectKey.isNotBlank()) {
                                val outputObject = apiDocumentObjectMap[caseOutputObjectKey]
                                if (outputObject != null) {
                                    val outputObjectJson = JsonUtil.objectToJson(outputObject, emptyArray())
                                    it.write("\t\tcase output:\t$outputObjectJson")
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
}