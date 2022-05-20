package com.tj.core.utils.concurrent;

import com.tj.core.utils.JsonUtil;
import com.tj.core.utils.concurrent.exception.TaskExecuteException;
import com.tj.core.utils.concurrent.executor.AbstractSyncTaskExecutor;
import com.tj.core.utils.concurrent.executor.SyncTaskExecutor;
import com.tj.core.utils.concurrent.param.TaskParam;
import com.tj.core.utils.concurrent.param.TaskResult;
import com.tj.core.utils.concurrent.threadarrange.ExecutorType;
import com.tj.core.utils.concurrent.threadarrange.TaskArranger;
import com.tj.core.utils.concurrent.threadarrange.TaskContainer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * demo
 *
 * @author tangjie
 * @date 2022/5/20
 */
@Slf4j
public class Demo {

    Executor executor = new ThreadPoolExecutor(10, 20, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000));

    public void test() throws TaskExecuteException, InterruptedException {
        List<SyncTaskExecutor<Object, Object>> taskExecutorList = new ArrayList<>();

        taskExecutorList.add(new TaskExecutor1(new TaskParam<>(1, "1")));
        taskExecutorList.add(new TaskExecutor2(new TaskParam<>(2, 2)));
        taskExecutorList.add(new TaskExecutor3(new TaskParam<>(3, 3)));

        TaskContainer<Object, Object> taskContainer = new TaskContainer<>();
        taskContainer.setSyncTaskExecutors(taskExecutorList);
        taskContainer.setThreadPool(executor);
        taskContainer.setExecutorType(ExecutorType.PARALLEL);


        List<SyncTaskExecutor<Object, Object>> taskExecutorList2 = new ArrayList<>();

        taskExecutorList2.add(new TaskExecutor1(new TaskParam<>(4, "1")));
        taskExecutorList2.add(new TaskExecutor2(new TaskParam<>(5, 2)));
        taskExecutorList2.add(new TaskExecutor3(new TaskParam<>(6, 3)));
        TaskContainer<Object, Object> taskContainer2 = new TaskContainer<>();
        taskContainer2.setSyncTaskExecutors(taskExecutorList2);
        taskContainer2.setThreadPool(executor);
        taskContainer2.setExecutorType(ExecutorType.PARALLEL);

        long start = System.currentTimeMillis();
        Map<TaskParam<Object>, TaskResult<Object>> map = TaskArranger.build().appendTask(taskContainer).appendTask(taskContainer2).doTasks();
        System.out.println(System.currentTimeMillis() - start);
        for (Map.Entry<TaskParam<Object>, TaskResult<Object>> entry : map.entrySet()) {
            System.out.println(JsonUtil.toJson(entry.getKey()));
            System.out.println(JsonUtil.toJson(entry.getValue()));
        }
    }

    public static void main(String[] args) throws TaskExecuteException, InterruptedException {
        new Demo().test();
    }


    static class TaskExecutor1 extends AbstractSyncTaskExecutor<String, String> {


        public TaskExecutor1(TaskParam<String> param) {
            super(param);
        }

        @Override
        public String invoke(String param) {
            return "任务执行器1:" + param;
        }
    }

    static class TaskExecutor2 extends AbstractSyncTaskExecutor<Integer, String> {


        public TaskExecutor2(TaskParam<Integer> param) {
            super(param);
        }

        @Override
        public String invoke(Integer param) {
            return "任务执行器2:" + param;
        }
    }

    static class TaskExecutor3 extends AbstractSyncTaskExecutor<Integer, Integer> {


        public TaskExecutor3(TaskParam<Integer> param) {
            super(param);
        }

        @Override
        public Integer invoke(Integer param) {
            //   throw  new RuntimeException("异常");
            try {
                Thread.sleep(1010);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return param;
        }
    }
}
