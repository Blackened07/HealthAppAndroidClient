package com.blackened.healthapp.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


object LocalDateTimeSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String = decoder.decodeString()

    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
}
@Serializable
data class HealthRecordResponseDTO(
    val id: Long,
    val type: String,
    val value1: Double,
    val value2: Double?,
    val note: String?,

    @Serializable(with = LocalDateTimeSerializer::class)
    val timestamp: String,

    val userName: String
)

@Serializable
data class HealthRecordRequestDTO(
    val type: String,
    val value1: Double,
    val value2: Double?,
    val note: String?,
    val TIMESTAMP: Long,
    val OFFSET_ZONE: Int
)


interface HealthApiService{
    @GET("health-records/history/{targetId}")
    suspend fun getHistory(
        @Path("targetId") targetId: Long,
        @Query("type") type: String?,
        @Query("from") from: String?,
        @Query("to") to: String?
    ): List<HealthRecordResponseDTO>

    @POST("health-records/{targetId}")
    suspend fun createRecord(
        @Path("targetId") targetId: Long,
        @Body request: HealthRecordRequestDTO
    ): HealthRecordResponseDTO

    @DELETE("health-records/{recordId}")
    suspend fun deleteRecord(
        @Path("recordId") recordId: Long
    ): okhttp3.ResponseBody


    @PUT("health-records/{recordId}")
    suspend fun updateRecord(
        @Path("recordId") recordId: Long,
        @Body request: HealthRecordRequestDTO
    ): HealthRecordResponseDTO
}