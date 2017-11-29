package com.hua.media.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hjz on 2017/11/29 0029.
 * 保存List数据
 */
public class ListDataSavePreference {

    /**
     * List<String>
     * List<Map<String,Object>>
     * List<JavaBean>
     * @param key
     * @param dataList
     */
    public static <T> void setDataList(Context context, String preference, String key, List<T> dataList) {
        if (null == dataList || dataList.size() <= 0){
            return;
        }
        try{
            SharedPreferences sharedPreferences = context.getSharedPreferences(preference, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            JSONObject jsonObject = new JSONObject();
            Gson gson = new Gson();
            //转换成json数据，再保存
            String strJson = gson.toJson(dataList);
            jsonObject.put("data",strJson);
            editor.clear();
            editor.putString(key, jsonObject.toString());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取List
     */
    public static String getDataList(Context context, String preference, String key, String defaultValue) {
        if(context == null) {
            return null;
        }
        try{
            SharedPreferences sharedPreferences = context.getSharedPreferences(preference, 0);
            String strJson = sharedPreferences.getString(key, defaultValue);

            if (null == strJson) {
                return "";
            }
            JSONObject jsonObject = new JSONObject(strJson);
            return jsonObject.optString("data");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 获取List(不能返回自己想要结果)
     */
    public static <T> List<T> getDataList2(Context context, String preference, String key, String defaultValue) {
        if(context == null) {
            return null;
        }
        try{
            SharedPreferences sharedPreferences = context.getSharedPreferences(preference, 0);
            List<T> dataList=new ArrayList<T>();
            String strJson = sharedPreferences.getString(key, defaultValue);
            if (null == strJson) {
                return dataList;
            }
            JSONObject jsonObject = new JSONObject(strJson);
            String jsonStr = jsonObject.optString("data");
            dataList = JSON.parseObject(jsonStr, new TypeReference<ArrayList<T>>() {});
            return dataList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}
