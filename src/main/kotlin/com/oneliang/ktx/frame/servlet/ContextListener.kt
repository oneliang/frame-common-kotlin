package com.oneliang.ktx.frame.servlet

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ConfigurationFactory
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.util.*
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

class ContextListener : ServletContextListener {
    companion object {

        /**
         * ContextListener constant
         */
        private val logger = LoggerManager.getLogger(ContextListener::class)

        //	private static final String CONTEXT_PARAMETER_DBCONFIG="dbConfig";
        private const val CONTEXT_PARAMETER_CONFIGFILE = "configFile"
    }

    /**
     * when the server is shut down,close the connection pool
     */
    override fun contextDestroyed(servletContextEvent: ServletContextEvent) {
        val configurationContext = ConfigurationFactory.singletonConfigurationContext
        configurationContext.destroyAll()
    }

    /**
     * when the server is starting initial all thing
     */
    override fun contextInitialized(servletContextEvent: ServletContextEvent) {
        TimeZone.setDefault(TimeZone.getTimeZone(Constants.Timezone.ASIA_SHANGHAI))
        //System.setProperty(StaticVar.USER_TIMEZONE, StaticVar.TIMEZONE_ASIA_SHANGHAI);
        Locale.setDefault(Locale.CHINA)
        //		String dbConfig=servletContextEvent.getServletContext().getInitParameter(CONTEXT_PARAMETER_DBCONFIG);
        val configFile = servletContextEvent.servletContext.getInitParameter(CONTEXT_PARAMETER_CONFIGFILE)
        //real path
        val realPath = servletContextEvent.servletContext.getRealPath(Constants.String.BLANK)

        //config file
        if (configFile.isNotBlank()) {
            val configurationContext = ConfigurationFactory.singletonConfigurationContext
            try {
                var classRealPath = Thread.currentThread().contextClassLoader.getResource(Constants.String.BLANK).path
                classRealPath = File(classRealPath).absolutePath
                configurationContext.classesRealPath = classRealPath
                configurationContext.projectRealPath = realPath
                configurationContext.initialize(configFile)
                ConfigurationFactory.inject()
                ConfigurationFactory.afterInject()
            } catch (e: Exception) {
                e.printStackTrace()
                logger.error(Constants.Base.EXCEPTION, e)
            }

        } else {
            logger.error("config file is not found,please initial the config file")
        }
    }
}
