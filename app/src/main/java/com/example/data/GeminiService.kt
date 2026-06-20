package com.example.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = null,
    val responseModalities: List<String>? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content
)

interface GeminiApiService {
    @POST("api/v1/story/generate")
    suspend fun generateContent(
        @retrofit2.http.Header("Authorization") authHeader: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("api/v1/images/generate")
    suspend fun generateImages(
        @retrofit2.http.Header("Authorization") authHeader: String,
        @Body request: GenerateImagesRequest
    ): GenerateImagesResponse
}

@Serializable
data class GenerateImagesRequest(
    val prompt: String,
    val numberOfImages: Int = 1,
    val aspectRatio: String = "1:1",
    val outputMimeType: String = "image/jpeg"
)

@Serializable
data class GenerateImagesResponse(
    val generatedImages: List<GeneratedImage>? = null
)

@Serializable
data class GeneratedImage(
    val image: ImageContent? = null
)

@Serializable
data class ImageContent(
    val imageBytes: String? = null
)

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}
