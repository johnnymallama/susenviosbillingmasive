package co.com.surenvios.billingmasive.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtil {

    private static final Logger logger = LogManager.getLogger(LogUtil.class);

    private LogUtil() {
    }

    public static void trackInfo(String source, String message) {
        String log = String.format("INFO [source]=%1s, [message]=%3s", source, message);
        logger.info(log);
    }

    public static void trackError(String source, String typeError, String message, Throwable error) {
        String log = String.format("ERROR [source]=%1s, [type_error]=%2s, [message]=%3s", source, typeError, message);
        logger.error(log, error);
    }
}
