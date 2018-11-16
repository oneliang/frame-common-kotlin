package com.oneliang.ktx.frame.i18n

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.AbstractContext
import com.oneliang.ktx.util.common.matchPattern
import com.oneliang.ktx.util.file.FileUtil
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class MessageContext : AbstractContext() {

    companion object {
        private const val FILE_PATH = '/'
        private const val UNDERLINE = '_'
        private const val PARAMETER_PATH = "-P="
        private const val PARAMETER_RECURSION = "-R"
        private const val PARAMETER_FILE = "-F="

        private val messagePropertiesMap = mutableMapOf<String, Properties>()

        /**
         * get message properties
         * @param localeKey
         * @return Properties
         */
        fun getMessageProperties(localeKey: String): Properties? {
            return messagePropertiesMap[localeKey]
        }
    }

    override fun initialize(parameters: String) {
        try {
            val parameterArray = parameters.split(Constants.Symbol.COMMA)
            var isRecursion = false
            var directoryPath: String = Constants.String.BLANK
            var matchPatternName: String = Constants.String.BLANK
            for (parameter in parameterArray) {
                if (parameter == PARAMETER_RECURSION) {
                    isRecursion = true
                } else if (parameter.startsWith(PARAMETER_PATH)) {
                    directoryPath = parameter.replaceFirst(PARAMETER_PATH.toRegex(), Constants.String.BLANK)
                } else if (parameter.startsWith(PARAMETER_FILE)) {
                    matchPatternName = parameter.replaceFirst(PARAMETER_FILE.toRegex(), Constants.String.BLANK)
                }
            }
            val fullDirectoryPath = if (directoryPath.isNotBlank()) classesRealPath + directoryPath else classesRealPath
            val directoryFile = File(fullDirectoryPath)
            loadPropertiesFile(directoryFile, matchPatternName, isRecursion)
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }

    }

    /**
     * destroy
     */
    override fun destroy() {
        messagePropertiesMap.clear()
    }

    /**
     * loadPropertiesFile,none recursion version
     * @param directoryFile
     * @param matchPatternName
     * @param isRecursion
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun loadPropertiesFile(directoryFile: File, matchPatternName: String, isRecursion: Boolean) {
        val queue = ConcurrentLinkedQueue<File>()
        queue.add(directoryFile)
        var sign = false
        while (!queue.isEmpty()) {
            val file = queue.poll()
            val filename = file.name
            if (file.isDirectory && !sign) {
                val fileList = file.listFiles()
                if (fileList != null) {
                    for (subFile in fileList) {
                        queue.add(subFile)
                    }
                    //is not recursion
                    if (!isRecursion) {
                        sign = true
                    }
                }
            } else if (file.isFile) {
                if (filename.matchPattern(matchPatternName)) {
                    val properties = FileUtil.getProperties(file)
                    val key = filename.substring(filename.indexOf(UNDERLINE) + 1, filename.lastIndexOf(Constants.Symbol.DOT))
                    if (messagePropertiesMap.containsKey(key)) {
                        val messageProperties = messagePropertiesMap[key]!!
                        messageProperties.putAll(properties)
                    } else {
                        messagePropertiesMap[key] = properties
                    }
                }
            }
        }
    }

    /**
     * recursion version
     * @param directoryFile
     * @param directoryPath
     * @param matchPatternName
     * @param isRecursion
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun loadPropertiesFile(directoryFile: File, directoryPath: String, matchPatternName: String, isRecursion: Boolean) {
        if (directoryFile.isDirectory) {
            val fileList = directoryFile.listFiles()
            if (fileList != null) {
                for (file in fileList) {
                    val filename = file.name
                    if (file.isFile) {
                        if (filename.matchPattern(matchPatternName)) {
                            val properties = FileUtil.getProperties(directoryPath + filename)
                            val key = filename.substring(filename.indexOf(UNDERLINE) + 1, filename.lastIndexOf(Constants.Symbol.DOT))
                            if (messagePropertiesMap.containsKey(key)) {
                                val messageProperties = messagePropertiesMap[key]!!
                                messageProperties.putAll(properties)
                            } else {
                                messagePropertiesMap[key] = properties
                            }
                        }
                    } else {
                        if (isRecursion) {
                            loadPropertiesFile(file, directoryPath + filename + FILE_PATH, matchPatternName, isRecursion)
                        }
                    }
                }
            }
        }
    }
}