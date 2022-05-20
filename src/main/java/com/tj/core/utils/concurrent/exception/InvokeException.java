package com.tj.core.utils.concurrent.exception;

/**
 * 方法调用异常
 *
 * @author tangjie
 * @date 2022/5/20
 */
public class InvokeException extends RuntimeException {

    public InvokeException(String message, Throwable cause) {
        super(message, cause);
    }
}
