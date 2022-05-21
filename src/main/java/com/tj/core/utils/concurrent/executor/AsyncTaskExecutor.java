package com.tj.core.utils.concurrent.executor;

import com.tj.core.utils.concurrent.param.TaskParam;
import com.tj.core.utils.concurrent.param.TaskResult;

/**
 * 异步任务执行器
 *
 * @author tangjie
 * @date 2022/5/21
 */
public interface AsyncTaskExecutor<K, T, V> {

    /**
     * 提交异步任务
     *
     * @param param 请求参数
     * @return 异步任务轮询标识
     * @throws Throwable 异常信息
     */
    TaskResult<V> submit(TaskParam<K> param) throws Throwable;

    /**
     * 查询异步任务执行结果
     *
     * @param posParam 轮询标识
     * @return 执行结果
     * @throws Throwable 异常信息
     */
    TaskResult<T> query(V posParam) throws Throwable;

    /**
     * 判断执行结果是否成功
     *
     * @param result 执行结果
     * @return 成功或失败
     */
    boolean isExecuteSuccess(T result);

    /**
     * 获取任务执行的超时时间
     *
     * @return 异步任务执行的超时时间
     */
    int getExpireTime();

    /**
     * 获取任务参数
     *
     * @return 任务参数
     */
    TaskParam<K> getParam();
}
