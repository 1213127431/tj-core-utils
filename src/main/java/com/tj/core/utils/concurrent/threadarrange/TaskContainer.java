package com.tj.core.utils.concurrent.threadarrange;

import com.tj.core.utils.concurrent.executor.SyncTaskExecutor;
import lombok.Data;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * 同步任务容器
 *
 * @author tangjie
 * @date 2022/5/20
 */
@Data
public class TaskContainer<K, T> {

    /**
     * 同步任务执行器
     */
    private List<SyncTaskExecutor<K, T>> syncTaskExecutors;

    /**
     * 执行器类型
     */
    private ExecutorType executorType = ExecutorType.PARALLEL;

    /**
     * 线程池
     */
    private Executor threadPool;

}
