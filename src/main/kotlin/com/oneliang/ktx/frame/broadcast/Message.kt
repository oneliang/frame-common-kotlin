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
     * get object
     *
     * @param key
     * @return instance
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getObject(key: String): T {
        return this.map[key] as T ?: error("key:$key does not exist")
    }

    fun containsObjectKey(key: String): Boolean {
        return this.map.containsKey(key)
    }
}
