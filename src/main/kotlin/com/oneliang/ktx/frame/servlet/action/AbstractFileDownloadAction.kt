package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants
import com.oneliang.ktx.StaticVar
import com.oneliang.ktx.frame.servlet.ActionUtil
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

abstract class AbstractFileDownloadAction : CommonAction() {

    /**
     * download file
     * @param filename
     * @return boolean
     * @throws ActionExecuteException
     */
    @Throws(ActionExecuteException::class)
    protected fun download(filename: String): Boolean {
        val request = ActionUtil.servletRequest as HttpServletRequest
        val response = ActionUtil.servletResponse as HttpServletResponse
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

            val newFilename = String(filename.toByteArray(Charsets.UTF_8), Charsets.ISO_8859_1)
            (response as HttpServletResponse).addHeader(Constants.Http.HeaderKey.CONTENT_DISPOSITION, "attachment;filename=$newFilename")
            outputStream = response.getOutputStream()
            inputStream = FileInputStream(filePath)
            val buffer = ByteArray(Constants.Capacity.BYTES_PER_KB)
            var length = inputStream.read(buffer, 0, buffer.size)
            while (length != -1) {
                outputStream.write(buffer, 0, length)
                length = inputStream.read(buffer, 0, buffer.size)
            }
            outputStream.flush()
            result = true
        } catch (e: Exception) {
            throw ActionExecuteException(e)
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: Exception) {
                throw ActionExecuteException(e)
            }

        }
        return result
    }
}
