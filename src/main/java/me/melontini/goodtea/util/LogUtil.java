package me.melontini.goodtea.util;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtil {
    private static final String MODID = "good-tea";
    private static final Logger LOGGER = LogManager.getLogger("good-tea");

    private static final boolean dev = FabricLoader.getInstance().isDevelopmentEnvironment();

    public static void info(String string) {
        if (dev) LOGGER.info("[" + MODID + "] " + string);
    }
    public static void info(Object object) {
        if (dev) LOGGER.info("[" + MODID + "] " + object);
    }
    public static void info(String string, Object... params) {
        if (dev) LOGGER.info("[" + MODID + "] " + string, params);
    }

    public static void importantInfo(String string) {
        LOGGER.info("[" + MODID + "] " + string);
    }
    public static void importantInfo(Object object) {
        LOGGER.info("[" + MODID + "] " + object);
    }


    public static void warn(String string) {
        LOGGER.warn("[" + MODID + "] " + string);
    }
    public static void warn(Object object) {
        LOGGER.warn("[" + MODID + "] " + object);
    }
    public static void error(String string) {
        LOGGER.error("[" + MODID + "] " + string);
    }

    public static void error(String string, Object... params) {
        LOGGER.error("[" + MODID + "] " + string, params);
    }
    public static void error(Object object) {
        LOGGER.error("[" + MODID + "] " + object);
    }
}
