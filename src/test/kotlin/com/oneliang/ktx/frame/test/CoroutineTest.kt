package com.oneliang.ktx.frame.test

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


fun log(message: Any) {
    println("[${Thread.currentThread()}][$message]")
}

open class CoroutineTest {

    suspend fun suspendFun() {
        log(1)
        withContext(Dispatchers.IO) {
            log(2)
            suspendFun2()
            delay(1000)
            log(3)
        }
        log(4)
    }

    suspend fun suspendFun2() {
        log(11)
        withContext(Dispatchers.Default) {
            log(12)
            notSuspendFun2()
            delay(1000)
            log(13)
        }
        log(14)
    }

    fun notSuspendFun() {
        log(100)
    }

    fun notSuspendFun2() {
        log(200)
    }
}

fun <T : CoroutineTest> a(constructor: () -> T) {

}

object CustomCoroutineScope :CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = newFixedThreadPoolContext(4, "Custom")

}
fun main(args: Array<String>) {
    CustomCoroutineScope.launch {
        log(23498)
    }

    val coroutineTest = CoroutineTest()
    log(1000)
    GlobalScope.launch {
        log(1001)
        coroutineTest.suspendFun()
        coroutineTest.notSuspendFun()
        log(1002)
    }
    GlobalScope.launch {
        log(2000)
    }
    Thread.sleep(1000)
    GlobalScope.launch {
        log(3000)
    }
    Thread.sleep(1000)
    GlobalScope.launch {
        log(4000)
    }
    Thread.sleep(1000)
    GlobalScope.launch {
        log(5000)
    }
    log(1003)
    Thread.sleep(6000)
}