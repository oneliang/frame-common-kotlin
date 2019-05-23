package com.oneliang.ktx.frame.test

import kotlinx.coroutines.*


open class CoroutineTest {

    suspend fun suspendFun() {
        println("suspend fun")
        withContext(Dispatchers.IO) {
            suspendFun2()
            delay(1000)
        }
    }

    suspend fun suspendFun2() {
        println("suspend fun2")
        withContext(Dispatchers.Default) {
            notSuspendFun2()
            delay(1000)
        }
    }

    fun notSuspendFun() {
        println("not suspend fun")
    }

    fun notSuspendFun2() {
        println("not suspend fun2")
    }
}

fun <T : CoroutineTest> a(constructor: () -> T) {

}

fun main(args: Array<String>) {
    val coroutineTest = CoroutineTest()
    GlobalScope.launch {
        coroutineTest.suspendFun()
        coroutineTest.notSuspendFun()
    }
    Thread.sleep(3000)
}