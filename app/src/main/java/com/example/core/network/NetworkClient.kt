package com.example.core.network

import android.content.Context
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    private var apiService: PitchMetricsApiService? = null
    private var cookieJar: PersistentCookieJar? = null

    fun getCookieJar(context: Context): PersistentCookieJar {
        return cookieJar ?: synchronized(this) {
            val jar = PersistentCookieJar(context.applicationContext)
            cookieJar = jar
            jar
        }
    }

    fun getApiService(context: Context): PitchMetricsApiService {
        return apiService ?: synchronized(this) {
            val jar = getCookieJar(context)
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .cookieJar(jar)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(LenientStringAdapter())
                .add(LenientDoubleAdapter())
                .add(LenientIntAdapter())
                .add(LenientBooleanAdapter())
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val baseUrl = BuildConfig.BASE_URL.ifEmpty { "https://pitchmetrics.online/api/" }
            SafeApiLogger.logRequest(baseUrl, "CLIENT_INITIALIZATION")

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val service = retrofit.create(PitchMetricsApiService::class.java)
            apiService = service
            service
        }
    }
}
