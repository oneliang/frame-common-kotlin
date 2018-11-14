package com.oneliang.ktx.frame.bean

import com.oneliang.util.common.ClassUtil
import com.oneliang.util.common.StringUtil
import com.oneliang.util.common.ClassUtil.ClassProcessor
import com.oneliang.util.json.JsonArray
import com.oneliang.util.json.JsonObject
import com.oneliang.util.json.JsonUtil
import com.oneliang.util.json.JsonUtil.JsonProcessor

class Message<O : Any, I : Any> {
    /**
     * @return the success
     */
    /**
     * @param success the success to set
     */
    var isSuccess = false
    /**
     * @return the message
     */
    /**
     * @param message the message to set
     */
    var message: String? = null
    /**
     * @return the object
     */
    /**
     * @param object the object to set
     */
    var `object`: O? = null
    /**
     * @return the objectList
     */
    /**
     * @param objectList the objectList to set
     */
    var objectList: List<I>? = null
    /**
     * @return the otherInformation
     */
    /**
     * @param otherInformation the otherInformation to set
     */
    var otherInformation: String? = null

    constructor() {}

    /**
     * constructor
     * @param success
     * @param message
     * @param object
     * @param objectList
     */
    constructor(success: Boolean, message: String, `object`: O, objectList: List<I>, otherInformation: String) {
        this.isSuccess = success
        this.message = message
        this.`object` = `object`
        this.objectList = objectList
        this.otherInformation = otherInformation
    }

    /**
     * to json
     * @return String
     */
    fun toJson(): String {
        return JsonUtil.objectToJson(this)
    }

    /**
     * to json
     * @param jsonProcessor
     * @return String
     */
    fun toJson(jsonProcessor: JsonProcessor): String {
        return JsonUtil.objectToJson(this, arrayOf<String>(), jsonProcessor)
    }

    companion object {

        private val FIELD_SUCCESS = "success"
        private val FIELD_MESSAGE = "message"
        private val FIELD_OBJECT = "object"
        private val FIELD_OBJECT_LIST = "objectList"
        private val FIELD_OTHER_INFORMATION = "otherInformation"

        /**
         * simple class use
         * @param <O>
         * @param <I>
         * @param json
         * @param objectClass
         * @param listItemClass
         * @return Message
        </I></O> */
        fun <O : Any, I : Any> jsonToMessage(json: String, objectClass: Class<O>, listItemClass: Class<I>): Message<O, I> {
            return jsonToMessage(json, objectClass, listItemClass, ClassUtil.DEFAULT_CLASS_PROCESSOR)
        }

        /**
         * for special class use
         * @param <O>
         * @param <I>
         * @param json
         * @param objectClass
         * @param listItemClass
         * @param classProcessor
         * @return Message
        </I></O> */
        fun <O : Any, I : Any> jsonToMessage(json: String, objectClass: Class<O>, listItemClass: Class<I>, classProcessor: ClassProcessor): Message<O, I> {
            val message = Message<O, I>()
            if (StringUtil.isNotBlank(json)) {
                val jsonObject = JsonObject(json)
                message.isSuccess = java.lang.Boolean.parseBoolean(ClassUtil.changeType(Boolean::class.javaPrimitiveType, arrayOf<String>(jsonObject.get(FIELD_SUCCESS).toString())).toString())
                message.message = ClassUtil.changeType(String::class.java, arrayOf<String>(jsonObject.get(FIELD_MESSAGE).toString())).toString()
                var `object` = jsonObject.get(FIELD_OBJECT)
                if (`object` is JsonObject) {
                    val jsonObjectValue = `object` as JsonObject
                    val objectValue = JsonUtil.jsonObjectToObject(jsonObjectValue, objectClass, classProcessor)
                    message.`object` = objectValue
                }
                `object` = jsonObject.get(FIELD_OBJECT_LIST)
                if (`object` is JsonArray) {
                    val jsonArrayValue = `object` as JsonArray
                    message.objectList = JsonUtil.jsonArrayToList(jsonArrayValue, listItemClass, classProcessor)
                }
                message.otherInformation = ClassUtil.changeType(String::class.java, arrayOf<String>(jsonObject.get(FIELD_OTHER_INFORMATION).toString())).toString()
            }
            return message
        }

        /**
         * obtain success message
         * @param <O>
         * @param <I>
         * @param message
         * @return Message<O></O>,I>
        </I></O> */
        fun <O : Any, I : Any> obtainSuccessMessage(message: String): Message<O, I> {
            return Message<O, I>(true, message, null, null, null)
        }

        /**
         * obtain success message
         * @param <O>
         * @param <I>
         * @param message
         * @param object
         * @return Message<O></O>,I>
        </I></O> */
        fun <O : Any, I : Any> obtainSuccessMessage(message: String, `object`: O): Message<O, I> {
            return Message(true, message, `object`, null, null)
        }

        /**
         * obtain success message
         * @param <O>
         * @param <I>
         * @param message
         * @param objectList
         * @return Message<O></O>,I>
        </I></O> */
        fun <O : Any, I : Any> obtainSuccessMessage(message: String, objectList: List<I>): Message<O, I> {
            return Message<O, I>(true, message, null, objectList, null)
        }

        /**
         * obtain success message
         * @param <O>
         * @param <I>
         * @param message
         * @param object
         * @param objectList
         * @param otherInformation
         * @return Message<O></O>,I>
        </I></O> */
        fun <O : Any, I : Any> obtainSuccessMessage(message: String, `object`: O, objectList: List<I>, otherInformation: String): Message<O, I> {
            return Message(true, message, `object`, objectList, otherInformation)
        }

        /**
         * obtain failure message
         * @param <O>
         * @param <I>
         * @param message
         * @return Message<O></O>,I>
        </I></O> */
        fun <O : Any, I : Any> obtainFailureMessage(message: String): Message<O, I> {
            return Message<O, I>(false, message, null, null, null)
        }

        /**
         * obtain failure message
         * @param <O>
         * @param <I>
         * @param message
         * @param object
         * @param objectList
         * @param otherInformation
         * @return Message<O></O>,I>
        </I></O> */
        fun <O : Any, I : Any> obtainFailureMessage(message: String, `object`: O, objectList: List<I>, otherInformation: String): Message<O, I> {
            return Message(false, message, `object`, objectList, otherInformation)
        }
    }
}
