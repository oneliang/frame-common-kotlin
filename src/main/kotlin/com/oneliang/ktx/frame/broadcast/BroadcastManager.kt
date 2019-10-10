package com.oneliang.ktx.frame.broadcast

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList

class BroadcastManager : BroadcastSender, Runnable {
    companion object {
        private val logger = LoggerManager.getLogger(BroadcastManager::class)
    }

    protected val broadcastReceiverMap: MutableMap<String, MutableList<BroadcastReceiver>> = ConcurrentHashMap()
    protected val messageQueue: Queue<Message> = ConcurrentLinkedQueue()
    private val lock = Object()
    protected var thread: Thread? = null

    /**
     * start
     */
    @Synchronized
    fun start() {
        if (this.thread == null) {
            this.thread = Thread(this)
            this.thread?.start()
        }
    }

    /**
     * interrupt
     */
    @Synchronized
    fun interrupt() {
        if (this.thread != null) {
            this.thread?.interrupt()
            this.thread = null
        }
    }

    /**
     * register broadcast receiver
     *
     * @param actionFilter
     * @param broadcastReceiver
     */
    fun registerBroadcastReceiver(actionFilter: Array<String>, broadcastReceiver: BroadcastReceiver) {
        if (actionFilter.isEmpty()) {
            return
        }
        for (actionKey in actionFilter) {
            if (actionKey.isBlank()) {
                continue
            }
            var broadcastReceiverList: MutableList<BroadcastReceiver>? = null
            if (this.broadcastReceiverMap.containsKey(actionKey)) {
                broadcastReceiverList = this.broadcastReceiverMap[actionKey]
            } else {
                broadcastReceiverList = CopyOnWriteArrayList()
                this.broadcastReceiverMap[actionKey] = broadcastReceiverList
            }
            broadcastReceiverList!!.add(broadcastReceiver)
        }
    }

    /**
     * unregister broadcast receiver
     *
     * @param broadcastReceiver
     */
    fun unregisterBroadcastReceiver(broadcastReceiver: BroadcastReceiver?) {
        if (broadcastReceiver == null) {
            return
        }
        val iterator = broadcastReceiverMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val broadcastReceiverList = entry.value
            if (broadcastReceiverList.contains(broadcastReceiver)) {
                broadcastReceiverList.remove(broadcastReceiver)
            }
        }
    }

    /**
     * send broadcast message
     *
     * @param message
     */
    override fun sendBroadcast(message: Message) {
        this.messageQueue.add(message)
        synchronized(lock) {
            lock.notify()
        }
    }

    /**
     * run
     */
    override fun run() {
        try {
            while (!Thread.currentThread().isInterrupted) {
                if (!this.messageQueue.isEmpty()) {
                    val message = this.messageQueue.poll()
                    handleMessage(message)
                } else {
                    synchronized(lock) {
                        lock.wait()
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
            Thread.currentThread().interrupt()
        }

    }

    /**
     * handle message
     *
     * @param message
     */
    protected fun handleMessage(message: Message) {
        val actionList = message.actionList
        if (actionList.isEmpty()) {
            return
        }
        for (action in actionList) {
            if (!broadcastReceiverMap.containsKey(action)) {
                continue
            }
            val broadcastReceiverList = broadcastReceiverMap[action]
            if (broadcastReceiverList == null || broadcastReceiverList.isEmpty()) {
                continue
            }
            val classList = message.classList
            if (classList.isEmpty()) {
                for (broadcastReceiver in broadcastReceiverList) {
                    broadcastReceiver.receive(action, message)
                }
            } else {
                for (broadcastReceiver in broadcastReceiverList) {
                    if (classList.contains(broadcastReceiver::class)) {
                        broadcastReceiver.receive(action, message)
                    }
                }
            }
        }
    }
}
