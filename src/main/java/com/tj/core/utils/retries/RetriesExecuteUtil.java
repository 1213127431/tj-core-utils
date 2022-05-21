package com.tj.core.utils.retries;

/**
 * 任务重试执行工具类
 *
 * @author tangjie
 * @date 2022/5/21
 */
public class RetriesExecuteUtil {

    /**
     * 任务执行（若失败会尝试重试执行）
     *
     * @param executor        执行器
     * @param param           请求参数
     * @param retries         重试次数
     * @param retriesInterval 重试间隔
     * @return 任务执行结果
     * @throws Exception 异常信息
     */
    public static <T, K> K run(Executor<T, K> executor, T param, int retries, int retriesInterval) throws Exception {

        if (executor == null || retries < 0 || retriesInterval < 0) {
            throw new IllegalArgumentException("重试任务参数配置错误");
        }

        int temp = 0;

        K result = null;

        while (temp++ < retries + 1) {

            try {

                result = executor.run(param, temp);
                break;

            } catch (Exception e) {

                if (temp == retries + 1) {
                    throw e;
                }

                if (retriesInterval > 0) {

                    try {
                        Thread.sleep(retriesInterval);
                    } catch (InterruptedException ie) {

                    }
                }
            }
        }

        return result;
    }


    /**
     * 任务重试执行器
     */
    public interface Executor<T, K> {

        /**
         * 任务重试具体执行方法
         *
         * @param param   执行参数
         * @param retries 重试次数
         * @return 执行结果
         * @throws Exception 异常信息
         */
        K run(T param, int retries) throws Exception;

    }
}
