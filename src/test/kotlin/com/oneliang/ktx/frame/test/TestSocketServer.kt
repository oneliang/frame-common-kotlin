package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.socket.BaseTcpProcessor
import com.oneliang.ktx.frame.socket.SocketServer
import com.oneliang.ktx.util.file.FileUtil
import java.io.ByteArrayOutputStream

fun main() {
    val baseTcpProcessor = BaseTcpProcessor()
    SocketServer(9999, true).also {
        it.processor = { inputStream, outputStream ->
            val tcpPacket = baseTcpProcessor.receiveTcpPacket(inputStream)
            println(tcpPacket.body.size.toString() + "," + String(tcpPacket.body))
            println("read finished")
            outputStream.write("321".toByteArray())
            outputStream.flush()
        }
        it.start()
    }
}