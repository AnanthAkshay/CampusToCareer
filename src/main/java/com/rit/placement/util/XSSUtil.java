package com.rit.placement.util;

public class XSSUtil {
    /**
     * Escapes HTML characters in a string to prevent XSS attacks.
     * Equivalent to JSTL's fn:escapeXml or <c:out>.
     */
    public static String escape(Object input) {
        if (input == null) {
            return "";
        }
        String str = String.valueOf(input);
        StringBuilder escaped = new StringBuilder(str.length() + 16);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '<': escaped.append("&lt;"); break;
                case '>': escaped.append("&gt;"); break;
                case '&': escaped.append("&amp;"); break;
                case '"': escaped.append("&quot;"); break;
                case '\'': escaped.append("&#x27;"); break;
                default: escaped.append(c);
            }
        }
        return escaped.toString();
    }
}
