package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.socket.SocketServer
import com.oneliang.ktx.frame.socket.TcpPacket
import com.oneliang.ktx.frame.socket.TcpPacketProcessor
import com.oneliang.ktx.frame.socket.TcpStreamProcessor
import java.io.InputStream
import java.io.OutputStream

fun main() {
    SocketServer(9999, true).also {
        it.streamProcessor = object : TcpStreamProcessor() {
            override fun process(inputStream: InputStream, outputStream: OutputStream) {
                val tcpPacket = this.tcpPacketProcessor.receiveTcpPacket(inputStream)
                println(tcpPacket.body.size.toString() + "," + String(tcpPacket.body))
                println("receive finished")
                this.tcpPacketProcessor.sendTcpPacket(outputStream, TcpPacket(1, "321".toByteArray()))
                println("send finished")
            }
        }
        it.start()
    }
}