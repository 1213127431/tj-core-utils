package com.tj.core.utils.concurrent.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 任务执行异常
 *
 * @author tangjie
 * @date 2022/5/20
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskExecuteException extends Exception {
    /**
     * 子任务执行异常
     */
    private List<ChildTaskException> childTaskExceptions;

    public TaskExecuteException(List<ChildTaskException> childTaskExceptions) {
        this.childTaskExceptions = childTaskExceptions;
    }
}
