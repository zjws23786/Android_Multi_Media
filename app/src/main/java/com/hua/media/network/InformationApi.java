package com.hua.media.network;

import com.hua.media.bean.KuGouRawLyric;
import com.hua.media.bean.KuGouSearchLyricResult;
import com.hua.media.bean.SearchModel;

import java.util.HashMap;
import java.util.Map;

import retrofit2.http.FieldMap;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by hjz on 2017/11/23 0023.
 */

public class InformationApi extends BaseNetworkManager {
    protected static final InformationApi.InformationService service = getRetrofit().create(InformationApi.InformationService.class);

    private interface InformationService{

        @GET("index.php?controller=list&action=searchList&sort=date")
        Observable<SearchModel> getSearchList(@QueryMap Map<String, Object> map);

    }

    /**
     * 获取搜索资讯内容
     * @param subscriber
     * @param p
     * @param n
     * @param searchStr
     */
    public static void getSearchList(Subscriber<SearchModel> subscriber, String url, Integer p, Integer n, String searchStr) {
        Map requestParams = new HashMap();
        if (p != null) {requestParams.put("p",p);}
        if (n != null) {requestParams.put("n",n);}
        if (searchStr != null) {requestParams.put("wd",searchStr);}
        Observable observable = getRetrofitUrl(url).create(InformationApi.InformationService.class).getSearchList(requestParams);
        toSubscribe(observable,subscriber);


    }

}
