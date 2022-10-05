package com.davixdevelop.schem2obj.utilities;

import com.davixdevelop.schem2obj.SchemeToObj;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Pattern;

public class Utility {
    final static Log logger = LogFactory.getLog(SchemeToObj.class);

    public static Pattern TEXTURE_NAME_FROM_FILE = Pattern.compile("^(.*?)((?>_[a-z])|(?>))\\.png");

    public static void Log(String msg){
        logger.debug(msg);
        System.out.println(msg);
    }
}
