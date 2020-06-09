package com.gency.commons.file.json.util;

public class Unicode {
	public static String escape(String value) {
		
		if (value == null)
			return "";
		
	    char[] charValue = value.toCharArray();
	    
	    StringBuilder result = new StringBuilder();
	    for (char ch : charValue){
	        if (ch != '_' && !(ch >= '0' && '9' >= ch) && !(ch >= 'a' && 'z' >= ch) && !(ch >= 'A' && 'Z' >= ch)) {    
	            String unicodeCh = Integer.toHexString((int)ch);
	           
	            result.append("\\u");
	            for (int i = 0; i < 4 - unicodeCh.length(); i++) {
	                result.append("0");
	            }
	            result.append(unicodeCh);

	        } else {
	        	result.append(ch);
	        }
	        
	    }
	    
	    return result.toString();
	}
}
