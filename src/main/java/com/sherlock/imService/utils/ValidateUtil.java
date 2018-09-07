package com.sherlock.imService.utils;

import com.sherlock.imService.exception.ServiceException;

public class ValidateUtil {

    public static boolean validateAccount(String account){
    	if(account.length()<6){
    		throw new ServiceException("账号长度不能小于6位");
    	}
        String reg = "(?i)^[A-Za-z0-9]+$";
        boolean result = account.matches(reg);
        if (!result) {
        	throw new ServiceException("账号格式不正确，只能包含字母和数字");
		}
        return result;
    }
    
    public static boolean validatePassword(String pwd) {
    	if (pwd.length()<6) {
    		throw new ServiceException("密码长度不能小于6位");
		}
    	String reg = "(?i)^[A-Za-z0-9!@#$%&]+$";
        boolean result = pwd.matches(reg);
        if (!result) {
        	throw new ServiceException("密码格式不正确，只能包含字母，数字和以下特殊字符!@#$%&");
		}
        return result;
    }
    
    public static void main(String[] args) {
    	System.out.println(validateAccount("sdfasfd"));
    	System.out.println(validateAccount("sdfasfd".toUpperCase()));
    	System.out.println(validateAccount("sdfasfd1"));
    	System.out.println(validateAccount("sdfasfd1@"));
    	
    	System.out.println(validatePassword("sdfasfd"));
    	System.out.println(validatePassword("sdfasfd".toUpperCase()));
    	System.out.println(validatePassword("sdfasfd1"));
    	System.out.println(validatePassword("sdfasfd1@"));
    }
}
