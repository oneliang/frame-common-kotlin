package com.oneliang.ktx.frame.broadcast

import kotlin.reflect.KClass

class Message {

    val actionList = mutableListOf<String>()
    val classList = mutableListOf<KClass<*>>()
    private val map = mutableMapOf<String, Any>()

    /**
     * default constructor
     */
    constructor() {}

    /**
     * frequent used constructor
     * @param action
     */
    constructor(action: String) {
        this.addAction(action)
    }

    /**
     * add class
     * @param clazz
     */
    fun addClass(clazz: KClass<*>) {
        this.classList.add(clazz)
    }


    /**
     * add action
     * @param action
     */
    fun addAction(action: String) {
        this.actionList.add(action)
    }

    /**
     * put object
     *
     * @param key
     * @param instance
     */
    fun putObject(key: String, instance: Any) {
        this.map[key] = instance
    }

    /**
     * get instance
     *
     * @param key
     * @return instance
     */
    fun getObject(key: String): Any {
        return this.map[key] ?: Any()
    }
}
