package com.example.mycompose.retrofit;

import com.example.mycompose.model.CurrentWeatherResponse;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("weather")
    Single<CurrentWeatherResponse> getCurrentWeather(
            @Query("q") String q,
            @Query("appid") String appId
    );

    @GET("weather")
    Single<CurrentWeatherResponse> getCurrentWeatherDetails(
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("appid") String appId
    );
}
