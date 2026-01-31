package com.zky.common.exception;

import com.zky.common.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    public Response<String> exceptionHandler(HttpServletRequest req, Exception e) {
        logger.error("发生异常！原因是:", e);
        return Response.<String>builder()
                .code("500")
                .info("服务器内部错误: " + e.getMessage())
                .build();
    }

    @ExceptionHandler(value = RuntimeException.class)
    public Response<String> runtimeExceptionHandler(HttpServletRequest req, RuntimeException e) {
        logger.error("发生运行时异常！原因是:", e);
        return Response.<String>builder()
                .code("500")
                .info(e.getMessage())
                .build();
    }
}
