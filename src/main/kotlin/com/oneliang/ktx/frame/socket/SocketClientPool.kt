package com.oneliang.ktx.frame.socket

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.perform
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.resource.ResourcePool

class SocketClientPool : ResourcePool<SocketClient>() {
    companion object {
        private val logger = LoggerManager.getLogger(SocketClientPool::class)
    }

    lateinit var tcpPacketProcessor: TcpPacketProcessor

    override fun destroyResource(resource: SocketClient?) {
        resource?.close()
    }

    fun useSocketClient(block: (socketClient: SocketClient) -> Unit) {
        val socketClient = this.stableResource ?: return
        perform({
            block(socketClient)
            this.releaseStableResource(socketClient)
        }, failure = {
            logger.error(Constants.Base.EXCEPTION, it)
            this.releaseStableResource(socketClient, true)
        })
    }
}