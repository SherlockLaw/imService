package com.sherlock.imService.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sherlock.imService.common.Result;

@ControllerAdvice
public class ExceptionHandlerAdvice {
	private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

	@ExceptionHandler(value={Exception.class})
	@ResponseBody
	public Result handle(Exception e){
		log.error(e.getMessage(),e);
		Integer code = null;
		Result result;
		if (e instanceof ServiceException) {
			ServiceException ex = (ServiceException) e;
			code = ex.getResultCode();
			result = Result.exception(code, e.getMessage());
		} else {
			//TODO:服务异常，统一抛出异常信息，用户友好
			result = Result.exception(code, e.getMessage());
		}
		return result;
	}
}
