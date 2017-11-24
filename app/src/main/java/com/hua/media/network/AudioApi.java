package com.hua.media.network;

import com.hua.media.bean.KuGouRawLyric;
import com.hua.media.bean.KuGouSearchLyricResult;

import java.io.File;
import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by hjz on 2017/11/23 0023.
 */

public class AudioApi extends BaseNetworkManager {
    protected static final AudioApi.AudioService service = getRetrofit().create(AudioApi.AudioService.class);

    private interface AudioService{

        @GET("search?ver=1&man=yes&client=pc")
        Observable<KuGouSearchLyricResult> searchLyric(@Query("keyword") String songName, @Query("duration") String duration);

        @GET("download?ver=1&client=pc&fmt=lrc&charset=utf8")
        Observable<KuGouRawLyric> getRawLyric(@Query("id") String id, @Query("accesskey") String accesskey);

    }

    public static void searchLyric(Subscriber<KuGouSearchLyricResult> subscriber, String title, String artist, long duration) {
        Observable observable = service.searchLyric(title, String.valueOf(duration));
        toSubscribe(observable,subscriber);
    }

    public static void getRawLyric(Subscriber<KuGouRawLyric> subscriber, String id, String accesskey) {
        Observable observable = service.getRawLyric(id, accesskey);
        toSubscribe(observable,subscriber);
    }

}
