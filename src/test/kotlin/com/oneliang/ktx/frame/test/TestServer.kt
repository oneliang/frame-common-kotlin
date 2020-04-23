package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.communication.Server

fun main() {
    val server = Server("localhost", 9999)
    server.processor = {
        println("server read:" + String(it))
        "server say: hello".toByteArray()
    }
    server.start()
}