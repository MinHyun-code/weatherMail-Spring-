package sample.prac.common.util;

import javax.servlet.http.HttpServletRequest;

public class CommonUtil {
	
	// ��üũ
    public static String paramNullCheck(HttpServletRequest request, String paramName, String returnVal) {
        return request.getParameter(paramName)==null?returnVal:request.getParameter(paramName);
    }
    
	// ��üũ
    public static String nullCheck(String paramName, String returnVal) {
        return paramName==null?returnVal:paramName;
    }
}
