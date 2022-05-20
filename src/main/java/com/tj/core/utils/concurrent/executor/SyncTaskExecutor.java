package com.tj.core.utils.concurrent.executor;

import com.tj.core.utils.concurrent.param.TaskParam;
import com.tj.core.utils.concurrent.param.TaskResult;

/**
 * 同步任务执行器
 *
 * @author tangjie
 * @date 2022/5/20
 */
public interface SyncTaskExecutor<K, T> {

    /**
     * 获取执行参数
     *
     * @return 执行参数
     */
    TaskParam<K> getParam();

    /**
     * 执行任务
     *
     * @param param 参数
     * @return 执行结果
     * @throws Throwable 异常信息
     */
    TaskResult<T> execute(TaskParam<K> param) throws Throwable;
}
