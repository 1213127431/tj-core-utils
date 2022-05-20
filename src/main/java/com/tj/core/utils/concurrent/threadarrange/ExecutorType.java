package com.tj.core.utils.concurrent.threadarrange;

/**
 * 容器任务类型枚举
 *
 * @author tangjie
 * @date 2022/5/20
 */
public enum ExecutorType {
    /**
     * 并行
     */
    PARALLEL(0, "并行"),

    /**
     * 串行
     */
    SERIAL(1, "串行"),
    ;

    ExecutorType(Integer type, String msg) {
    }
}
