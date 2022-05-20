package com.tj.core.utils;

import com.alibaba.fastjson2.JSON;

/**
 * json工具类
 *
 * @author tangjie
 * @version 0.0.1
 * @since 2022/5/3 11:04
 **/
public class JsonUtil {

    public static String toJson(Object obj) {
        return JSON.toJSONString(obj);
    }

}
