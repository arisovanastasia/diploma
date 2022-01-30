package com.example.retrorally.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

private const val BASE_URL = "https://kkozhakin.pythonanywhere.com/api/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

interface RetroRallyApiService {
    @GET("users")
    suspend fun getJudgeWithData(): Response<ContestDataDTO>

    @GET("protocols/{id}")
    suspend fun getProtocolById()

    @POST("protocols")
    suspend fun postItemInProtocol()

    @PUT("protocols/{id}")
    suspend fun putItemInProtocolById()
}

object RetroRallyApi {
    val retrofitService: RetroRallyApiService by lazy {
        retrofit.create(RetroRallyApiService::class.java)
    }
}