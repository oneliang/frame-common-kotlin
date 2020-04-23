package com.oneliang.ktx.frame.communication

import com.oneliang.ktx.util.common.MD5String
import com.oneliang.ktx.util.common.perform
import com.oneliang.ktx.util.concurrent.ThreadTask
import com.oneliang.ktx.util.logging.LoggerManager
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.*

open class SelectorThreadTask(val selector: Selector) : ThreadTask {
    companion object {
        private val logger = LoggerManager.getLogger(SelectorThreadTask::class)
    }

    internal var processor: (byteArray: ByteArray) -> ByteArray = { ByteArray(0) }

    override fun runTask() {
        while (true) {
            this.selector.select()//blocking
            logger.info("selector:%s", this.selector)
            val keysIterator = selector.selectedKeys().iterator()
            while (keysIterator.hasNext()) {
                val key = keysIterator.next()
                keysIterator.remove()
                when {
                    key.isReadable -> {
                        val socketChannel = key.channel() as SocketChannel
                        val address = socketChannel.remoteAddress as InetSocketAddress
                        logger.debug("connected, time:%s, host:%s, port:%s", Date(), address.hostString, address.port)
                        perform({
                            val byteArray = socketChannel.readByteArray()
                            val responseByteArray = this.processor(byteArray)
                            socketChannel.write(ByteBuffer.wrap(responseByteArray))
                            logger.debug("byte array md5:%s, byte array size:%s", byteArray.MD5String(), byteArray.size)
                        }) {
                            logger.error("disconnect", it)
                            key.cancel()
                            socketChannel.close()
                        }
                    }
                }
            }
        }
    }
}