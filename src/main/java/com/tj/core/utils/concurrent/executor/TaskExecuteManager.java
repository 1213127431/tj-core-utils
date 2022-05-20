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

}
