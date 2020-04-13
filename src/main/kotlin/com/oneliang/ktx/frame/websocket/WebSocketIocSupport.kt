package com.oneliang.ktx.frame.websocket

import com.oneliang.ktx.frame.ConfigurationFactory
import com.oneliang.ktx.frame.ioc.IocContext
import com.oneliang.ktx.frame.ioc.autoInjectObjectById

@Suppress("LeakingThis")
abstract class WebSocketIocSupport {
    init {
        ConfigurationFactory.singletonConfigurationContext.autoInjectObjectById(this::class.java.name, this)
    }
}