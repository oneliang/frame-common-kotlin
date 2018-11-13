package com.oneliang.ktx.frame.servlet.action

import java.io.InputStream
import java.util.ArrayList

import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.oneliang.Constants
import com.oneliang.StaticVar
import com.oneliang.frame.servlet.ActionUtil
import com.oneliang.util.common.StringUtil
import com.oneliang.util.upload.FileUpload
import com.oneliang.util.upload.FileUploadResult

abstract class AbstractFileUploadAction : CommonAction() {

    /**
     * file upload from request,just for form submit
     * @return List<FileUploadResult>
     * @throws Exception
    </FileUploadResult> */
    @Throws(Exception::class)
    protected fun upload(): List<FileUploadResult>? {
        return this.upload(null)
    }

    /**
     * file upload from request,just for form submit
     * @param saveFilenames
     * @return List<FileUploadResult>
     * @throws ActionExecuteException
    </FileUploadResult> */
    @Throws(ActionExecuteException::class)
    protected fun upload(saveFilenames: Array<String>?): List<FileUploadResult>? {
        val request = ActionUtil.getServletRequest() as HttpServletRequest
        val response = ActionUtil.getServletResponse() as HttpServletResponse
        return this.upload(request, response, saveFilenames)
    }

    /**
     * file upload from request.getInputStream(),for inputStream submit
     * @param request
     * @param response
     * @param saveFilenames
     * @return List<FileUploadResult>
     * @throws ActionExecuteException
    </FileUploadResult> */
    @Throws(ActionExecuteException::class)
    @JvmOverloads
    protected fun upload(request: ServletRequest, response: ServletResponse, saveFilenames: Array<String>? = null): List<FileUploadResult>? {
        return this.upload(request, response, null, saveFilenames)
    }

    /**
     * file upload from request.getInputStream(),for inputStream submit
     * @param request
     * @param response
     * @param fileFullName
     * @return List<FileUploadResult>
     * @throws ActionExecuteException
    </FileUploadResult> */
    @Throws(ActionExecuteException::class)
    protected fun upload(request: ServletRequest, response: ServletResponse, fileFullName: String?, saveFilenames: Array<String>?): List<FileUploadResult>? {
        response.setContentType(Constants.Http.ContentType.TEXT_PLAIN)
        // get content type of client request
        val contentType = request.getContentType()
        var fileUploadResultList: MutableList<FileUploadResult>? = null
        try {
            if (contentType != null) {
                val inputStream = request.getInputStream()
                val fileUpload = FileUpload()
                val filePath = StaticVar.UPLOAD_FOLDER
                fileUpload.setSaveFilePath(filePath)
                // make sure content type is multipart/form-data,form file use

                if (contentType!!.indexOf(Constants.Http.ContentType.MULTIPART_FORM_DATA) >= 0) {
                    fileUploadResultList = fileUpload.upload(inputStream, request.getContentLength(), saveFilenames)
                } else if (contentType!!.indexOf(Constants.Http.ContentType.APPLICATION_OCTET_STREAM) >= 0 || contentType!!.indexOf(Constants.Http.ContentType.BINARY_OCTET_STREAM) >= 0) {
                    if (StringUtil.isNotBlank(fileFullName)) {
                        fileUploadResultList = ArrayList<FileUploadResult>()
                        val fileUploadResult = fileUpload.upload(inputStream, fileFullName)
                        fileUploadResultList.add(fileUploadResult)
                    }
                }// make sure content type is application/octet-stream or binary/octet-stream,flash jpeg picture use or file upload use
            }
        } catch (e: Exception) {
            throw ActionExecuteException(e)
        }

        return fileUploadResultList
    }

    companion object {

        /**
         * serialVersionUID
         */
        private val serialVersionUID = -4867033632772727310L
    }
}
/**
 * file upload from request,just for form submit
 * @param request
 * @param response
 * @return List<FileUploadResult>
 * @throws ActionExecuteException
</FileUploadResult> */
