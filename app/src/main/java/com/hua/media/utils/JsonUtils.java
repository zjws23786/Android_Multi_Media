package com.hua.media.utils;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Type;

/**
 * Created by btwo on 2016/10/14.
 */
public class JsonUtils {

    public static <T> T fromJson(String json, Type tClass){
        T t = null;
        try {
            t = JSON.parseObject(json, tClass);
        } catch (Exception e) {
        }
        return t;
    }

}
