package com.hua.media.network;

import com.hua.media.MyApplication;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hjz on 2017/11/23 0023.
 */

public abstract class BaseNetworkManager {
    public  static String API_SERVER = "http://lyrics.kugou.com/";

    private static OkHttpClient mOkHttpClient;
    private static Retrofit mRetrofit;
    private static Retrofit.Builder mRetrofitBuilder;
    protected static Retrofit getRetrofit() {
        initOkHttpClient();
        if (mRetrofit == null) {
            //构建Retrofit
            mRetrofit = new Retrofit.Builder()
                    //配置服务器路径
                    .baseUrl(API_SERVER)
                    //配置转化库，默认是Gson
                    .addConverterFactory(FastJsonConverterFactory.create())
                    //配置回调库，采用RxJava
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    //设置OKHttpClient为网络客户端
                    .client(mOkHttpClient)
                    .build();
        }
        return mRetrofit;
    }

    protected static Retrofit getRetrofitUrl(String url) {
        initOkHttpClient();
        if (mRetrofitBuilder == null) {
            //构建Retrofit
            mRetrofitBuilder = new Retrofit.Builder()
                    //配置服务器路径
//                    .baseUrl(API_SERVER)
                    //配置转化库，默认是Gson
                    .addConverterFactory(FastJsonConverterFactory.create())
                    //配置回调库，采用RxJava
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    //设置OKHttpClient为网络客户端
                    .client(mOkHttpClient);
        }
        mRetrofitBuilder.baseUrl(url);
        return mRetrofitBuilder.build();
    }

    /**
     * 初始化OKHTTP
     */

    private static void initOkHttpClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (mOkHttpClient == null) {
            synchronized (BaseNetworkManager.class) {
                if (mOkHttpClient == null) {
                    // 指定缓存路径,缓存大小100Mb
                    Cache cache = new Cache(new File(MyApplication.getInstance().getCacheDir(), "HttpCache"),
                            1024 * 1024 * 100);
                    mOkHttpClient = new OkHttpClient.Builder()
                            .cache(cache)
                            .cookieJar(new CookiesManager())
                            .addInterceptor(interceptor)
//                            .addInterceptor(new ApplicationInterceptors())
                            .retryOnConnectionFailure(true)
                            .connectTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20,TimeUnit.SECONDS)
                            .build();
                }
            }
        }
    }

    /**
     * 自动管理Cookies
     */
    public static class CookiesManager implements CookieJar {
        private final PersistentCookieStore cookieStore = new PersistentCookieStore(MyApplication.getInstance());

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            if (cookies != null && cookies.size() > 0) {
                for (Cookie item : cookies) {
                    cookieStore.add(url, item);
                }
            }
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url);
            return cookies;
        }
    }

    //订阅
    protected static <T> void toSubscribe(Observable<T> o, Subscriber<T> s) {
        o.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s);
    }
}
