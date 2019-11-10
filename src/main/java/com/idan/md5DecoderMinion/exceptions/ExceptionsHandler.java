package com.idan.md5DecoderMinion.exceptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@ResponseBody
@ControllerAdvice
public class ExceptionsHandler {
    private static final Logger logger = LogManager.getLogger(ExceptionsHandler.class);

    @ExceptionHandler(ApplicationException.class)
    public ApplicationException handleApplicationException(HttpServletResponse response, ApplicationException exception) {
        response.setStatus(exception.getErrorType().getNumber());
        logger.error(exception);
        return exception;
    }
}
