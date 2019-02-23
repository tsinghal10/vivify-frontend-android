package com.allandroidprojects.ecomsample.utility;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface Api {

    @FormUrlEncoded
    @POST("login/")
    Call<ResponseBody> login(
            @Field("username") String username,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("register/")
    Call<ResponseBody> register(
            @Field("email") String email,
            @Field("username") String username,
            @Field("password") String password
    );

    @GET("custom_products/")
    Call<ResponseBody> get_products();

    @GET("products/{id}/")
    Call<ResponseBody> get_product_details(@Path("id") String id);

    @FormUrlEncoded
    @POST("qr/")
    Call<ResponseBody> authenticateQR(
            @Field("id") int id
    );
}
