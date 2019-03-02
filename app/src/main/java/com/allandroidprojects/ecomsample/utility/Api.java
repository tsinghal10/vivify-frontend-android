package com.allandroidprojects.ecomsample.utility;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Api {

    @FormUrlEncoded
    @POST("login/")
    Call<ResponseBody> login(
            @Field("username") String username,
            @Field("password") String password
    );

    @DELETE("login/")
    Call<ResponseBody> logout();

    @FormUrlEncoded
    @POST("register/")
    Call<ResponseBody> register(
            @Field("email") String email,
            @Field("username") String username,
            @Field("password") String password
    );

    @GET("custom_products/")
    Call<ResponseBody> get_products(
            @Query("page") int page,
            @Query("cat") String q);

    @GET("custom_products/")
    Call<ResponseBody> get_search_products(
            @Query("page") int page,
            @Query("q") String q);

    @GET("products/{id}/")
    Call<ResponseBody> get_product_details(
            @Path("id") String id,
            @Query("email") String email);

    @FormUrlEncoded
    @POST("qr/")
    Call<ResponseBody> authenticateQR(
            @Field("id") int id
    );

    @FormUrlEncoded
    @POST("basket/myadd/{id}/")
    Call<ResponseBody> add_to_cart(
            @Path("id") String id,
            @Field("quantity") int quantity,
            @Field("email") String email);

    // Get request to get cart list
    @GET("basket/")
    Call<ResponseBody> get_cart(@Query("email") String email);

    @GET("basket/{id}/lines/")
    Call<ResponseBody> get_cart_list(@Path("id") String id,
                                     @Query("email") String email);

    @GET("orders/{id}/lines/")
    Call<ResponseBody> get_orders(@Path("id") String id,
                                     @Query("email") String email);

    @FormUrlEncoded
    @POST("checkout/")
    Call<ResponseBody> checkout(@Query("email") String email, @Field("basket") String basket);

    @GET("orders/")
    Call<ResponseBody> order_list(@Query("email") String email);

}
