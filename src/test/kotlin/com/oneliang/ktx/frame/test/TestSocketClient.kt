package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.socket.BaseTcpProcessor
import com.oneliang.ktx.frame.socket.TcpPacket
import com.oneliang.ktx.util.common.toHexString
import java.net.Socket


fun main() {
    val baseTcpProcessor = BaseTcpProcessor()
    val client = Socket("127.0.0.1", 9999)
    val inputStream = client.getInputStream()
    val outputStream = client.getOutputStream()
    val tcpPacket = TcpPacket(1, "123".toByteArray())
    println(tcpPacket.toByteArray().toHexString())
    baseTcpProcessor.send(outputStream, tcpPacket.toByteArray())
    while (true) {
        if (inputStream.available() > 0) {
            val byteArray = ByteArray(inputStream.available())
            inputStream.read(byteArray)
            println(String(byteArray))
            break
        }
    }
}