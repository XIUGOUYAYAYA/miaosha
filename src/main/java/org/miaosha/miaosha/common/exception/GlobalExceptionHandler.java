package org.miaosha.miaosha.common.exception;

import lombok.Getter;
import org.miaosha.miaosha.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局统一异常处理器
 * 拦截所有Controller抛出的异常，统一包装成你现有的Result返回格式
 */

@RestControllerAdvice
public class GlobalExceptionHandler {


    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 捕获自定义业务异常 BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        // 使用你Result里的error方法，传入异常信息
        return Result.error(e.getMessage());
    }



    /**
     * @RequestBody JSON参数校验异常（@NotNull @NotBlank）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errMsg = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("接口参数校验失败：{}", errMsg);
        return Result.error(errMsg);
    }

    /**
     * form表单参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String errMsg = e.getFieldError().getDefaultMessage();
        log.warn("表单参数绑定异常：{}", errMsg);
        return Result.error(errMsg);
    }

    /**
     * 兜底捕获所有其他未知异常（空指针、数据库异常等）
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleAllException(Exception e) {
        log.error("系统未知异常", e);
        return Result.error("服务器内部错误，请联系管理员");
    }

}
