package com.oneliang.ktx.frame.socket

import com.oneliang.ktx.util.common.readWithBuffer
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.common.writeWithBuffer
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class BaseTcpProcessor(private val typeByteArrayLength: Int = 4, private val bodyLengthByteArrayLength: Int = 4) {

    @Throws(Exception::class)
    fun send(outputStream: OutputStream, byteArray: ByteArray) {
        outputStream.writeWithBuffer(byteArray)
    }

    @Throws(Exception::class)
    fun send(outputStream: OutputStream, type: Int, body: ByteArray) {
        sendTcpPacket(outputStream, TcpPacket(type, body))
    }

    @Throws(Exception::class)
    fun sendTcpPacket(outputStream: OutputStream, tcpPacket: TcpPacket) {
        println(tcpPacket.toByteArray().size)
        send(outputStream, tcpPacket.toByteArray())
    }

    @Throws(Exception::class)
    fun receive(inputStream: InputStream): ByteArray {
        return receiveTcpPacket(inputStream).toByteArray()
    }

    @Throws(Exception::class)
    fun receiveType(inputStream: InputStream): ByteArray {
        return inputStream.readWithBuffer(this.typeByteArrayLength)
    }

    @Throws(Exception::class)
    fun receiveBody(inputStream: InputStream): ByteArray {
        val bodyOutputStream = ByteArrayOutputStream()
        this.receiveBody(inputStream, bodyOutputStream)
        return bodyOutputStream.toByteArray()
    }

    @Throws(Exception::class)
    fun receiveBody(inputStream: InputStream, outputStream: OutputStream) {
        val bodyLengthByteArray = inputStream.readWithBuffer(this.bodyLengthByteArrayLength)
        val bodyLength: Int = bodyLengthByteArray.toInt()
        inputStream.readWithBuffer(bodyLength, outputStream)
    }

    @Throws(Exception::class)
    fun receiveTcpPacket(inputStream: InputStream): TcpPacket {
        val type = receiveType(inputStream)
        val bodyByteArray = this.receiveBody(inputStream)
        return TcpPacket(type.toInt(), bodyByteArray)
    }
}