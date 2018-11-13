package com.oneliang.ktx.frame.servlet.action

import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.oneliang.Constants
import com.oneliang.StaticVar
import com.oneliang.frame.servlet.ActionUtil

abstract class AbstractFileDownloadAction : CommonAction() {

    /**
     * download file
     * @param filename
     * @return boolean
     * @throws ActionExecuteException
     */
    @Throws(ActionExecuteException::class)
    protected fun download(filename: String): Boolean {
        val request = ActionUtil.getServletRequest() as HttpServletRequest
        val response = ActionUtil.getServletResponse() as HttpServletResponse
        return this.download(request, response, filename)
    }

    /**
     * download file
     * @param request
     * @param response
     * @param filename
     * @throws ActionExecuteException
     */
    @Throws(ActionExecuteException::class)
    protected fun download(request: ServletRequest, response: ServletResponse, filename: String): Boolean {
        var result = false
        response.setContentType(Constants.Http.ContentType.APPLICATION_X_DOWNLOAD)
        val filePath = StaticVar.DOWNLOAD_FOLDER + filename
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {

            val newFilename = String(filename.getBytes(Constants.Encoding.GB2312), Constants.Encoding.ISO88591)
            (response as HttpServletResponse).addHeader(Constants.Http.HeaderKey.CONTENT_DISPOSITION, "attachment;filename=$newFilename")
            outputStream = response.getOutputStream()
            inputStream = FileInputStream(filePath)
            val buffer = ByteArray(Constants.Capacity.BYTES_PER_KB)
            var length = -1
            while ((length = inputStream!!.read(buffer, 0, buffer.size)) != -1) {
                outputStream!!.write(buffer, 0, length)
            }
            outputStream!!.flush()
            result = true
        } catch (e: Exception) {
            throw ActionExecuteException(e)
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close()
                    inputStream = null
                }
                if (outputStream != null) {
                    outputStream.close()
                    outputStream = null
                }
            } catch (e: Exception) {
                throw ActionExecuteException(e)
            }

        }
        return result
    }
}
