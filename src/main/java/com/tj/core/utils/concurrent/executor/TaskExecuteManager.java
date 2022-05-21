package com.tj.core.utils.concurrent.executor;

import com.tj.core.utils.JsonUtil;
import com.tj.core.utils.concurrent.param.TaskParam;
import com.tj.core.utils.concurrent.param.TaskResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 任务执行管理器
 *
 * @author tangjie
 * @date 2022/5/20
 */
@Slf4j
public class TaskExecuteManager {

    /**
     * 提交同步任务执行器
     *
     * @param executor             线程池
     * @param syncTaskExecutorList 任务执行器列表
     * @param <K>                  任务请求参数类型，需继承TaskParam类
     * @param <T>                  任务返回结果
     * @return 任务执行结果
     */
    public static <K, T> Map<TaskParam<K>, TaskResult<T>> submitSync(Executor executor, List<SyncTaskExecutor<K, T>> syncTaskExecutorList) throws InterruptedException {
        // 校验执行器不能为空
        if (syncTaskExecutorList == null || syncTaskExecutorList.size() == 0) {

            throw new IllegalArgumentException("syncTaskExecutorList不能为空");
        }

        // 校验执行器中参数不能为空
        for (SyncTaskExecutor<K, T> syncTaskExecutor : syncTaskExecutorList) {

            if (syncTaskExecutor.getParam() == null || syncTaskExecutor.getParam().getId() == null) {

                throw new IllegalArgumentException("taskParam不能为空");
            }
        }

        // 校验任务唯一标识不能重复
        Set<Integer> taskIdSet = syncTaskExecutorList.stream().map(x -> x.getParam().getId()).collect(Collectors.toSet());
        if (taskIdSet.size() != syncTaskExecutorList.size()) {
            throw new IllegalArgumentException("任务唯一标识Id不能重复");
        }

        Map<TaskParam<K>, TaskResult<T>> resultMap = new ConcurrentHashMap<>(20);

        int count = syncTaskExecutorList.size();
        CountDownLatch countDownLatch = new CountDownLatch(count);

        for (SyncTaskExecutor<K, T> syncTaskExecutor : syncTaskExecutorList) {

            executor.execute(() -> {

                TaskParam<K> param = syncTaskExecutor.getParam();

                TaskResult<T> taskResult = null;
                long startTime = System.currentTimeMillis();

                try {

                    taskResult = syncTaskExecutor.execute(param);
                    resultMap.put(param, taskResult);

                } catch (Throwable e) {

                    log.error("任务器执行异常,执行器名称:[{}],任务参数信息:[{}]", syncTaskExecutor.getClass().getName(), JsonUtil.toJson(param), e);
                    taskResult = new TaskResult<>();
                    taskResult.setException(true);
                    taskResult.setExceptionMsg(e);
                    resultMap.put(param, taskResult);

                } finally {

                    if (taskResult != null) {
                        taskResult.setResponseTime(System.currentTimeMillis() - startTime);
                    }
                    countDownLatch.countDown();

                }
            });

        }

        countDownLatch.await();
        return resultMap;
    }

    /**
     * 提交异步任务执行器
     *
     * @param executor              线程池
     * @param asyncTaskExecutorList 异步任务执行器
     * @param <K>                   taskParam中请求参数类型
     * @param <T>                   taskResult中出参类型
     * @param <V>                   提交异步请求返回的轮询标识类型
     * @return 执行结果
     * @throws InterruptedException 线程中断异常
     */
    public static <K, T, V> Map<TaskParam<K>, TaskResult<T>> submitAsync(Executor executor, List<AsyncTaskExecutor<K, T, V>> asyncTaskExecutorList) throws InterruptedException {
        // 校验执行器不能为空
        if (asyncTaskExecutorList == null || asyncTaskExecutorList.size() == 0) {

            throw new IllegalArgumentException("asyncTaskExecutorList不能为空");
        }

        // 校验执行器中参数不能为空
        for (AsyncTaskExecutor<K, T, V> asyncTaskExecutor : asyncTaskExecutorList) {

            if (asyncTaskExecutor.getParam() == null || asyncTaskExecutor.getParam().getId() == null) {

                throw new IllegalArgumentException("taskParam不能为空");
            }

            if (asyncTaskExecutor.getExpireTime() <= 1) {

                throw new IllegalArgumentException("expireTime必须大于1");
            }
        }

        // 校验任务唯一标识不能重复
        Set<Integer> taskIdSet = asyncTaskExecutorList.stream().map(x -> x.getParam().getId()).collect(Collectors.toSet());
        if (taskIdSet.size() != asyncTaskExecutorList.size()) {
            throw new IllegalArgumentException("任务唯一标识Id不能重复");
        }

        Map<TaskParam<K>, TaskResult<T>> resultMap = new ConcurrentHashMap<>(20);

        int count = asyncTaskExecutorList.size();
        CountDownLatch countDownLatch = new CountDownLatch(count);

        for (AsyncTaskExecutor<K, T, V> asyncTaskExecutor : asyncTaskExecutorList) {

            executor.execute(() -> {

                long startTime = System.currentTimeMillis();
                TaskResult<T> lastRet = null;

                TaskParam<K> taskParam = asyncTaskExecutor.getParam();

                try {

                    TaskResult<V> ret = asyncTaskExecutor.submit(taskParam);

                    // 每秒轮询一次
                    int pollingTimes = asyncTaskExecutor.getExpireTime() / 1;
                    // 执行计数器
                    int countFlag = 0;
                    // 轮询是否成功
                    boolean regFlag = false;

                    while (countFlag++ < pollingTimes && !regFlag) {

                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {

                        }

                        // 此处查询结果发生异常则终止查询
                        lastRet = asyncTaskExecutor.query(ret.getData());
                        regFlag = asyncTaskExecutor.isExecuteSuccess(lastRet.getData());
                    }

                    if (lastRet != null) {

                        lastRet.setPollingExpired(!regFlag);
                    }

                    resultMap.put(taskParam, lastRet);

                } catch (Throwable e) {

                    log.error("异步任务执行异常,执行器名称:[{}],任务参数信息:[{}]", asyncTaskExecutor.getClass().getName(), JsonUtil.toJson(taskParam), e);
                    lastRet = new TaskResult<>();
                    lastRet.setException(true);
                    lastRet.setExceptionMsg(e);
                    resultMap.put(taskParam, lastRet);

                } finally {
                    if (lastRet != null) {
                        lastRet.setResponseTime(System.currentTimeMillis() - startTime);
                    }
                    countDownLatch.countDown();

                }
            });
        }

        countDownLatch.await();
        return resultMap;
    }
}
