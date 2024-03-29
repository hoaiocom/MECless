package com.hoant.mecless;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadAPIs {
//    @Multipart
//    @POST("faas-pigo")
//    Call<PigoResponse> uploadImage(
//            @Part MultipartBody.Part file);
    @Multipart
    @POST("faas-pigo")
    Call<PigoResponse> uploadImage(
        @Part List<MultipartBody.Part> files);
}
