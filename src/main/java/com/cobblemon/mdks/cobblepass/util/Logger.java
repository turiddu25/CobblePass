package com.cobblemon.mdks.cobblepass.util;

import org.apache.logging.log4j.LogManager;

public class Logger {
    private final org.apache.logging.log4j.Logger logger;
    private final String prefix;

    public Logger(String modId) {
        this.logger = LogManager.getLogger(modId);
        this.prefix = "[" + modId + "] ";
    }

    public void info(String message) {
        logger.info(prefix + message);
    }

    public void warn(String message) {
        logger.warn(prefix + message);
    }

    public void error(String message) {
        logger.error(prefix + message);
    }

    public void error(String message, Throwable t) {
        logger.error(prefix + message, t);
    }

    public void fatal(String message) {
        logger.fatal(prefix + message);
    }

    public void fatal(String message, Throwable t) {
        logger.fatal(prefix + message, t);
    }

    public void debug(String message) {
        logger.debug(prefix + message);
    }
}
