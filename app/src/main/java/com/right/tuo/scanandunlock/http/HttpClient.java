package com.right.tuo.scanandunlock.http;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 描述
 *
 * @author jacktuotuo
 */
public class HttpClient {

    private Retrofit mRetrofit;

    private HttpClient() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build();

        mRetrofit = new Retrofit.Builder()
                .client(okHttpClient)
//                .baseUrl("http://test.app.rightsidetech.com:10083/")
//                .baseUrl("http://192.168.1.203:9088/")
                .baseUrl("http://check.rightsidetech.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static HttpClient getInstance() {
        return Holder.INSTANCE;
    }


    private static class Holder {
        private static final HttpClient INSTANCE = new HttpClient();
    }


    public Retrofit getRetrofit() {
        return mRetrofit;
    }


}
