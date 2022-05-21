package com.tj.core.utils.concurrent.threadpool;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通用线程创建器
 *
 * @author tangjie
 * @date 2022/5/21
 */
public class CommonThreadPoolExecutors {

    /**
     * 创建一个固定数量的线程池
     *
     * @param poolName       线程池名称
     * @param corePoolSize   核心线程数
     * @param blockQueueSize 阻塞队列大小
     * @return 核心线程数与最大线程数相等的线程池
     */
    public static ExecutorService newFixedThreadPool(String poolName, int corePoolSize, int blockQueueSize) {

        if (poolName == null || corePoolSize <= 0 || blockQueueSize <= 0) {
            throw new IllegalArgumentException("线程池参数配置错误");
        }

        return new ThreadPoolExecutor(corePoolSize, corePoolSize, 10, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(blockQueueSize), new DefaultThreadFactory(poolName), new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 创建一个核心线程数为最大线程数一半的线程池
     *
     * @param poolName       线程池名称
     * @param maxPoolSize    最大线程数
     * @param blockQueueSize 阻塞队列大小
     * @return 核心线程数为最大线程数一半的线程池
     */
    public static ExecutorService newHalfMaxThreadPool(String poolName, int maxPoolSize, int blockQueueSize) {

        if (poolName == null || maxPoolSize <= 0 || blockQueueSize <= 0) {
            throw new IllegalArgumentException("线程池参数配置错误");
        }

        int corePoolSize = maxPoolSize / 2 == 0 ? 1 : maxPoolSize / 2;

        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, 10, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(blockQueueSize), new DefaultThreadFactory(poolName), new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * The default thread factory
     */
    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory(String poolName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = poolName + "-pool-" +
                    POOL_NUMBER.getAndIncrement() +
                    "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
