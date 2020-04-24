package com.oneliang.ktx.frame.socket

import com.oneliang.ktx.util.logging.LoggerManager
import java.net.Socket

class SocketClient(private val host: String, private val port: Int) {
    companion object {
        private val logger = LoggerManager.getLogger(SocketClient::class)
    }

    private val socket = Socket(this.host, this.port)

    lateinit var tcpPacketProcessor: TcpPacketProcessor

    fun send(tcpPacket: TcpPacket): TcpPacket {
        val outputStream = this.socket.getOutputStream()
        val inputStream = this.socket.getInputStream()
        this.tcpPacketProcessor.sendTcpPacket(outputStream, tcpPacket)
        return this.tcpPacketProcessor.receiveTcpPacket(inputStream)
    }

    fun close() {
        this.socket.close()
    }
}