package com.tj.core.utils.concurrent.param;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 抽象任务参数
 *
 * @author tangjie
 * @date 2022/5/20
 */
@Getter
@Setter
@AllArgsConstructor
public class TaskParam<T> {

    /**
     * 任务唯一标识
     */
    private Integer id;

    /**
     * 任务具体参数
     */
    private T param;


    /**
     * 重写equals和hashCode方法 id相同表示任务相同
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * 重写equals和hashCode方法 id相同表示任务相同
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof TaskParam)) {

            return false;
        }

        return getId().equals(((TaskParam<?>) obj).getId());
    }
}
