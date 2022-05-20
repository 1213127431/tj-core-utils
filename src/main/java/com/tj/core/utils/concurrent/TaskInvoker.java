package com.tj.core.utils.concurrent;


import com.tj.core.utils.concurrent.exception.InvokeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 任务调用
 *
 * @author tangjie
 * @date 2022/5/20
 */
public class TaskInvoker {

    /**
     * 通过反射调用目标方法
     *
     * @param target     目标类
     * @param methodName 方法名称
     * @param param      方法请求参数
     * @return 执行结果
     */
    public static Object invoke(Object target, String methodName, Object param) {
        try {
            Class<?> paramType = param.getClass();
            Method method = target.getClass().getMethod(methodName, paramType);

            return method.invoke(target, param);
        } catch (InvocationTargetException e) {
            throw new InvokeException(String.format("[%s]方法[%s]调用异常", target.getClass().getName(), methodName), e.getTargetException());
        } catch (Exception e) {
            throw new InvokeException(String.format("[%s]方法[%s]调用异常", target.getClass().getName(), methodName), e);
        }
    }
}
