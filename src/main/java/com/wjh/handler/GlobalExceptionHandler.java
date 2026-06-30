package com.wjh.handler;


import com.wjh.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler (RuntimeException runtimeException){
        log.info(runtimeException.toString(), runtimeException);
        return Result.error("服务器异常");
    }

}
