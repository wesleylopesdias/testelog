package com.logicalis.serviceinsight.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.apache.commons.lang.StringUtils;

public class VersionUtil {
    
    private static final NumberFormat versionFormatter = DecimalFormat.getInstance();
    
    static {
        versionFormatter.setMaximumFractionDigits(2);
        versionFormatter.setMinimumFractionDigits(2);
    }

    /**
     * should format a Double into a "version" looking string, like 1.11 becomes 1.1.1
     * 
     * @param version
     * @return 
     */
    public static String formatVersion(Double version) {
        if (version == null) {
            return null;
        }
        String versionString = versionFormatter.format(version); // should yield something like x.xx
        return new StringBuilder(versionString.substring(0, versionString.length()-1))
                .append(".")
                .append(versionString.substring(versionString.length()-1, versionString.length()))
                .toString();
    }
    
    /**
     * expects a String of the form 0.1.0 or similar and converts to a Double value
     * 
     * @param s_version
     * @return 
     */
    public static Double parseVersion(String s_version) {
        
        if (StringUtils.isBlank(s_version)) {
            return null;
        }
        String[] parts = s_version.split("\\.");
        if (parts.length < 2) {
            return Double.valueOf(s_version);
        }
        StringBuilder builder = new StringBuilder(parts[0]).append(".");
        for (int i=1; i<parts.length; i++) {
            builder.append(parts[i]);
        }
        return Double.valueOf(builder.toString());
    }
}
