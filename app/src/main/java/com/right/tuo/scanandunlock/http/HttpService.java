package com.right.tuo.scanandunlock.http;


import com.right.tuo.scanandunlock.bean.BaseResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


/**
 * 描述
 *
 * @author jacktuotuo
 */
public interface HttpService {

    // 车辆扫码开锁
    @FormUrlEncoded
    @POST("check/bicycle/lend")
    Call<BaseResponse<String>> openLock(@Field("bicycleNo") String bicycleNo);


    // 车辆扫码上锁
    @FormUrlEncoded
    @POST("check/bicycle/lock")
    Call<BaseResponse<String>> closeLock(@Field("bicycleNo") String bicycleNo);


    // 批量车辆绑定
    @FormUrlEncoded
    @POST("bicycle/no/batchBindSensorsNo")
    Call<BaseResponse<String>> batchBindSensorsNo(@Field("token") String token, @Field("data") String data);

}
