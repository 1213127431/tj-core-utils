package com.tj.core.utils.concurrent.executor;

import com.tj.core.utils.concurrent.param.TaskParam;
import com.tj.core.utils.concurrent.param.TaskResult;

/**
 * 抽象同步任务执行器
 *
 * @author tangjie
 * @date 2022/5/20
 */
public abstract class AbstractSyncTaskExecutor<K, T> implements SyncTaskExecutor {

    public AbstractSyncTaskExecutor(TaskParam<K> param) {
        this.param = param;
    }

    private final TaskParam<K> param;

    /**
     * 获取执行参数
     *
     * @return 执行参数
     */
    @Override
    public TaskParam<K> getParam() {
        return param;
    }

    /**
     * 执行任务
     *
     * @param param 参数
     * @return 执行结果
     */
    @Override
    public TaskResult execute(TaskParam param) throws Throwable {
        T data = invoke((K) param.getParam());
        TaskResult<T> taskResult = new TaskResult<>();
        taskResult.setData((T) data);
        return taskResult;
    }

    /**
     * 子执行器具体方法执行
     *
     * @param param 请求参数
     * @return 执行结果
     * @throws Throwable 异常信息
     */
    public abstract T invoke(K param) throws Throwable;

}
