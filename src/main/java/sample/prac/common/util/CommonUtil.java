package sample.prac.common.util;

import javax.servlet.http.HttpServletRequest;

public class CommonUtil {
	
	// 널체크
    public static String paramNullCheck(HttpServletRequest request, String paramName, String returnVal) {
        return request.getParameter(paramName)==null?returnVal:request.getParameter(paramName);
    }
    
	// 널체크
    public static String nullCheck(String paramName, String returnVal) {
        return paramName==null?returnVal:paramName;
    }
}
