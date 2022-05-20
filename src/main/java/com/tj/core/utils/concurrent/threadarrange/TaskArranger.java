package com.tj.core.utils.concurrent.threadarrange;

import com.tj.core.utils.JsonUtil;
import com.tj.core.utils.concurrent.exception.ChildTaskException;
import com.tj.core.utils.concurrent.exception.TaskExecuteException;
import com.tj.core.utils.concurrent.executor.SyncTaskExecutor;
import com.tj.core.utils.concurrent.executor.TaskExecuteManager;
import com.tj.core.utils.concurrent.param.TaskParam;
import com.tj.core.utils.concurrent.param.TaskResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务编排器
 *
 * @author tangjie
 * @date 2022/5/20
 */
@Slf4j
public class TaskArranger {
    private TaskArranger() {
    }

    /**
     * 创建一个任务编排器
     *
     * @return 任务编排器
     */
    public static TaskArranger build() {
        return new TaskArranger();
    }

    /**
     * 任务容器
     */
    private List<TaskContainer> taskContainers;

    private boolean isExecuted = false;

    /**
     * 添加一个任务容器至任务编排器
     *
     * @param taskContainer 任务容器
     * @return 任务编排器实例
     */
    public TaskArranger appendTask(TaskContainer taskContainer) {

        if (taskContainer == null) {

            throw new IllegalArgumentException("taskContainer 不能为空");
        }

        if (taskContainer.getSyncTaskExecutors() == null || taskContainer.getSyncTaskExecutors().size() == 0) {

            throw new IllegalArgumentException("syncTaskExecutors 不能为空");
        }

        if (taskContainer.getThreadPool() == null) {

            throw new IllegalArgumentException("threadPool 不能为空");
        }

        for (Object object : taskContainer.getSyncTaskExecutors()) {

            SyncTaskExecutor syncTaskExecutor = (SyncTaskExecutor) object;

            if (syncTaskExecutor == null || syncTaskExecutor.getParam() == null || syncTaskExecutor.getParam().getId() == null) {

                throw new IllegalArgumentException("syncTaskExecutor 和 taskParam 不能为空");
            }
        }


        if (taskContainers == null) {

            taskContainers = new ArrayList<>();
        }

        // 检查任务是否重复
        if (taskContainers.size() > 0) {

            for (int i = 0; i < taskContainer.getSyncTaskExecutors().size(); i++) {

                SyncTaskExecutor syncTaskExecutor = (SyncTaskExecutor) taskContainer.getSyncTaskExecutors().get(i);

                for (TaskContainer oldContainer : taskContainers) {

                    if (oldContainer.getSyncTaskExecutors().stream().anyMatch(x ->
                            ((SyncTaskExecutor) x).getParam().getId().equals(syncTaskExecutor.getParam().getId()))) {

                        throw new IllegalArgumentException(String.format("任务Id不能重复[%s]", syncTaskExecutor.getParam().getId()));
                    }

                }
            }
        }

        taskContainers.add(taskContainer);
        return this;
    }

    /**
     * 任务执行
     *
     * @return 任务执行结果
     * @throws TaskExecuteException 任务执行异常信息
     * @throws InterruptedException 线程中断异常
     */
    public <K, T> Map<TaskParam<K>, TaskResult<T>> doTasks() throws TaskExecuteException, InterruptedException {

        if (isExecuted) {

            throw new IllegalArgumentException("任务已被执行，不能重复执行");
        }

        if (taskContainers == null || taskContainers.size() == 0) {

            throw new IllegalArgumentException("任务容器taskContainers不能为空");
        }

        isExecuted = true;

        Map<TaskParam<K>, TaskResult<T>> resultMap = new ConcurrentHashMap<>(20);

        for (TaskContainer taskContainer : taskContainers) {

            doTask(resultMap, taskContainer);
        }

        return resultMap;
    }

    /**
     * 任务执行
     *
     * @param resultMap     任务执行结果
     * @param taskContainer 任务容器
     * @throws TaskExecuteException 任务执行异常
     * @throws InterruptedException 线程中断异常
     */
    private <K, T> void doTask(Map<TaskParam<K>, TaskResult<T>> resultMap, TaskContainer<K, T> taskContainer) throws TaskExecuteException, InterruptedException {
        // 只有一个任务时，不需要线程池调度
        if (taskContainer.getSyncTaskExecutors().size() == 1) {

            SyncTaskExecutor<K, T> syncTaskExecutor = taskContainer.getSyncTaskExecutors().get(0);

            Map<TaskParam<K>, TaskResult<T>> taskResultMap = singleExecute(syncTaskExecutor);

            checkTaskExceptions(taskResultMap);

            resultMap.putAll(taskResultMap);

            return;
        }

        // 并行执行
        if (ExecutorType.PARALLEL == taskContainer.getExecutorType()) {

            Map<TaskParam<K>, TaskResult<T>> taskResultMap = TaskExecuteManager.submitSync(taskContainer.getThreadPool(), taskContainer.getSyncTaskExecutors());

            checkTaskExceptions(taskResultMap);

            resultMap.putAll(taskResultMap);

        } else { // 串行执行

            for (SyncTaskExecutor<K, T> syncTaskExecutor : taskContainer.getSyncTaskExecutors()) {

                Map<TaskParam<K>, TaskResult<T>> taskResultMap = singleExecute(syncTaskExecutor);

                checkTaskExceptions(taskResultMap);

                resultMap.putAll(taskResultMap);

            }

        }

    }

    /**
     * 检查结果集的任务异常
     *
     * @param taskResultMap 任务执行结果
     * @throws TaskExecuteException 任务执行异常信息
     */
    private <K, T> void checkTaskExceptions(Map<TaskParam<K>, TaskResult<T>> taskResultMap) throws TaskExecuteException {
        List<ChildTaskException> taskExceptions = null;

        for (Map.Entry<TaskParam<K>, TaskResult<T>> entry : taskResultMap.entrySet()) {
            TaskParam<K> taskParam = entry.getKey();
            TaskResult<T> taskResult = entry.getValue();

            if (taskResult.isException()) {

                if (taskExceptions == null) {

                    taskExceptions = new ArrayList<>();

                }

                Throwable throwable = taskResult.getExceptionMsg();

                if (throwable instanceof TaskExecuteException) {

                    TaskExecuteException taskExecuteException = (TaskExecuteException) throwable;

                    taskExceptions.addAll(taskExecuteException.getChildTaskExceptions());

                } else {

                    taskExceptions.add(new ChildTaskException(taskParam.getId(), throwable));
                }
            }
        }

        if (taskExceptions != null && taskExceptions.size() > 0) {

            throw new TaskExecuteException(taskExceptions);
        }
    }

    /**
     * 单个执行器执行
     *
     * @param syncTaskExecutor 同步任务执行器
     * @return 执行结果
     */
    private <K, T> Map<TaskParam<K>, TaskResult<T>> singleExecute(SyncTaskExecutor<K, T> syncTaskExecutor) {

        Map<TaskParam<K>, TaskResult<T>> retMap = new ConcurrentHashMap<>(20);

        TaskParam<K> param = syncTaskExecutor.getParam();

        long startTime = System.currentTimeMillis();

        TaskResult<T> ret = null;

        try {

            ret = syncTaskExecutor.execute(param);
            retMap.put(param, ret);

        } catch (Throwable e) {

            log.error("任务器执行异常,执行器名称:[{}],任务参数信息:[{}]", syncTaskExecutor.getClass().getName(), JsonUtil.toJson(param), e);
            ret = new TaskResult<>();
            ret.setException(true);
            ret.setExceptionMsg(e);
            retMap.put(param, ret);

        } finally {

            if (ret != null) {
                ret.setResponseTime(System.currentTimeMillis() - startTime);
            }
        }

        return retMap;
    }
}
