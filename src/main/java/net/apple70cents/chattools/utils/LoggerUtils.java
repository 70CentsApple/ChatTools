package net.apple70cents.chattools.utils;

//#if MC>=11800
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//#endif

public class LoggerUtils {
    //#if MC>=11800
    public static Logger LOGGER;
    //#endif

    public static void init(){
        //#if MC>=11800
        LOGGER = LoggerFactory.getLogger("chattools");
        //#endif
    }
    public static void info(String s){
        //#if MC>=11800
        LOGGER.info(s);
        //#else
        //$$ System.out.println(s);
        //#endif
    }
    public static void warn(String s){
        //#if MC>=11800
        LOGGER.warn(s);
        //#else
        //$$ System.out.println(s);
        //#endif
    }
    public static void error(String s){
        //#if MC>=11800
        LOGGER.error(s);
        //#else
        //$$ System.err.println(s);
        //#endif
    }
}
