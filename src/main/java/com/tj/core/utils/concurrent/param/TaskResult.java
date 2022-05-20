package com.tj.core.utils.concurrent.param;

import lombok.Data;

/**
 * 任务结果
 *
 * @author tangjie
 * @date 2022/5/20
 */
@Data
public class TaskResult<T> {

    /**
     * 任务执行中是否发生异常
     */
    private boolean isException;

    /**
     * 任务执行异常
     */
    private Throwable exceptionMsg = null;

    /**
     * 异步任务轮询是否超时
     */
    private boolean isPollingExpired;

    /**
     * 接口响应时间，单位毫秒
     */
    private Long responseTime;

    /**
     * 具体数据
     */
    private T data;

}
