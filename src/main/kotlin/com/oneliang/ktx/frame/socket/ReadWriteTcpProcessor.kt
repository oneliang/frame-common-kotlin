package com.oneliang.ktx.frame.socket

import com.oneliang.ktx.util.logging.LoggerManager
import java.io.InputStream
import java.io.OutputStream

class ReadWriteTcpProcessor : StreamProcessor {
    companion object {
        private val logger = LoggerManager.getLogger(ReadWriteTcpProcessor::class)
    }

    private val baseTcpProcessor = BaseTcpProcessor()
    override fun process(inputStream: InputStream, outputStream: OutputStream) {
        val tcpPacket = this.baseTcpProcessor.receiveTcpPacket(inputStream)
        logger.info("read finished, type:%s, body size:%s", tcpPacket.type, tcpPacket.body.size)
//        this.baseTcpProcessor.send(outputStream)
        outputStream.flush()
    }
}