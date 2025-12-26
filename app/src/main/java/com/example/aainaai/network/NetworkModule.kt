package com.example.aainaai.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Data classes for API Payloads
 */
data class HashRequest(
    val name: String,
    val citizen_no: String,
    val dob: String
)

/**
 * Retrofit Service Interface
 */
interface AainaApiService {
    @POST("api/reset")
    suspend fun resetState()

    @Headers("Content-Type: application/json")
    @POST("api/hash")
    suspend fun sendHash(@Body payload: HashRequest)

    @retrofit2.http.Multipart
    @POST("api/scan")
    suspend fun sendScan(
        @retrofit2.http.Part front: okhttp3.MultipartBody.Part,
        @retrofit2.http.Part back: okhttp3.MultipartBody.Part
    )
}

data class ScanBase64Payload(
    val front: String,
    val back: String
)

interface SecondaryApiService {
    @Headers("Content-Type: application/json")
    @POST(".")
    suspend fun sendImages(@Body payload: ScanBase64Payload)
}

/**
 * Singleton Network Object
 */
object NetworkManager {
    // UPDATED BASE URL as requested
    private const val BASE_URL = "https://needed-narwhal-charmed.ngrok-free.app/"
    private const val BASE_URL_SECONDARY = "https://c0068e89ea7d.ngrok-free.app/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: AainaApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AainaApiService::class.java)

    val secondaryApi: SecondaryApiService = Retrofit.Builder()
        .baseUrl(BASE_URL_SECONDARY)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SecondaryApiService::class.java)
}

/**
 * Hashing Utility
 */
object HashUtils {
    fun generateSha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
