package com.example.retrorally.data.network

import com.example.retrorally.data.models.dto.ContestDataDTO
import com.example.retrorally.data.models.dto.ParticipantDTO
import com.example.retrorally.data.models.dto.ResponseDTO
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

private const val BASE_URL = "https://kkozhakin.pythonanywhere.com/api/"
private val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val client = OkHttpClient.Builder()
    .addInterceptor(interceptor)
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

interface RetroRallyApiService {
    @GET("protocols")
    suspend fun getJudgeWithData(
        @Query("login") password: String
    ): Response<ContestDataDTO>

    @POST("strings/")
    suspend fun postItemInProtocol(
        @Body data: ParticipantDTO
    ) : Response<ResponseDTO>

    @POST("strings/{id}")
    suspend fun postPhotoOfProtocols()
}

object RetroRallyApi {
    val retrofitService: RetroRallyApiService by lazy {
        retrofit.create(RetroRallyApiService::class.java)
    }
}