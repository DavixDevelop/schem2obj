package com.davixdevelop.schem2obj.util;

import com.davixdevelop.schem2obj.SchemeToObj;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Pattern;

public class LogUtility {
    final static Log logger = LogFactory.getLog(SchemeToObj.class);

    public static void Log(String msg){
        logger.debug(msg);
        System.out.println(msg);
    }
}
