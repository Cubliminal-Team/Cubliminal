package net.limit.cubliminal.util;

import net.limit.cubliminal.Cubliminal;
import org.apache.http.annotation.Obsolete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debug {
    private static final String MOD_ID = Cubliminal.MOD_ID;
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final String ANSI_BLUE    = "\u001B[34m";
    public static final String ANSI_YELLOW  = "\u001B[33m";
    public static final String ANSI_CYAN    = "\u001B[36m";
    public static final String ANSI_MAGENTO = "\u001B[35m";
    public static final String ANSI_GREEN   = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET   = "\u001B[0m";

    /**
     * Outputs a debug message to the Minecraft log for debugging purposes.
     * @param msg The message to include in the debug message.
     */
    public static void debug(Object msg){
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String loc = caller.getClassName() + "." + caller.getMethodName() + "()" + ANSI_MAGENTO + " [Line " + caller.getLineNumber() + "]";
        LOGGER.warn(ANSI_YELLOW + "{DEBUG | " + ANSI_CYAN + MOD_ID + ANSI_YELLOW + " | " + ANSI_BLUE + loc + ANSI_YELLOW + "}");
        String message = "%sMessage : %s%s";
        if (msg == null){
            LOGGER.error(String.format(message, ANSI_RED, "", msg));
        } else {
            LOGGER.info(String.format(message, ANSI_GREEN, ANSI_RESET, msg));
        }
        LOGGER.warn(ANSI_YELLOW + "-- DEBUG END --");
    }

    /**
     * Outputs multiple lines of messages.
     * @param msg The list of messages to display.
     */
    public static void debug(Object... msg){
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String loc = caller.getClassName() + "." + caller.getMethodName() + "() " + ANSI_MAGENTO + " [Line " + caller.getLineNumber() + "]" + ANSI_YELLOW;
        LOGGER.warn(ANSI_YELLOW + "{DEBUG | " + ANSI_CYAN + MOD_ID + ANSI_YELLOW + " | " + ANSI_BLUE + loc + ANSI_YELLOW + "}");
        for (int i = 0; i < msg.length; i++){
            String message = "%sMessage %s(Line %d)%s : %s";
            if (msg[i] == null){
                LOGGER.error(String.format(message, ANSI_RED, ANSI_MAGENTO, i, ANSI_RED, msg[i]));
            } else {
                LOGGER.info(String.format(message, ANSI_GREEN, ANSI_MAGENTO, i, ANSI_RESET, msg[i]));
            }
        }
        LOGGER.warn(ANSI_YELLOW + "-- DEBUG END --");
    }

    /**
     * Displays a debug message every tick. Useful for methods that would run every tick, that way the IDE can handle it.
     * @param age = Simply get the age of the entity.
     * @param increment = How many times per tick the message will display. 20 ticks = 1 second
     * @param msg = The message to display/
     */
    public static void debug(int age, int increment, Object msg){
        if (age % increment == 0){
            debug(msg);
        }
    }

    public static void debug(int age, int increment, Object... msg){
        if (age % increment == 0){
            debug(msg);
        }
    }

    /**
     * Displays a message to the Minecraft log to list sectors that was loaded from the mod.
     * @param c The class that has loaded.
     */
    public static void displayRegisteredSectors(Class c){
        LOGGER.info(ANSI_GREEN + "{ LOADED | " + ANSI_CYAN + MOD_ID + ANSI_GREEN + " } " + ANSI_BLUE + c.getSimpleName());
    }
}
