package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.socket.nio.Server
import com.oneliang.ktx.frame.socket.TcpPacket
import com.oneliang.ktx.frame.socket.TcpPacketProcessor
import com.oneliang.ktx.frame.socket.TcpStreamProcessor
import com.oneliang.ktx.util.common.toByteArray
import java.io.ByteArrayInputStream

fun main() {
    val tcpPacketProcessor = TcpPacketProcessor()
    val server = Server("localhost", 9999)
    server.processor = {
        val tcpPackage = tcpPacketProcessor.receiveTcpPacket(ByteArrayInputStream(it))
        val requestString = String(tcpPackage.body)
        println(Thread.currentThread().toString() + ", server read :$requestString")
        TcpPacket(1.toByteArray(), "server say: hello $requestString".toByteArray()).toByteArray()
    }
    server.start()
}