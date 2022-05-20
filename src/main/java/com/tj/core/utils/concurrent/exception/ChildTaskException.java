package com.tj.core.utils.concurrent.exception;

import lombok.Data;

/**
 * 子任务执行异常
 *
 * @author tangjie
 * @date 2022/5/20
 */
@Data
public class ChildTaskException {
    public ChildTaskException(Integer taskId, Throwable throwable) {
        this.taskId = taskId;
        this.throwable = throwable;
    }

    /**
     * 任务Id
     */
    private Integer taskId;

    /**
     * 任务执行异常
     */
    private Throwable throwable;


}
