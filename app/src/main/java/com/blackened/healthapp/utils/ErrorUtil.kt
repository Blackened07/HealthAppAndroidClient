package com.blackened.healthapp.utils

import com.blackened.healthapp.domain.model.ErrorResponseDTO
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.time.LocalDateTime

object ErrorUtil {
    fun getDefault(e: Exception): ErrorResponseDTO {
        return ErrorResponseDTO(
            timestamp = LocalDateTime.now().toString(),
            message = "Педорас: ${e.message}",
            status = 0,
            critical = false
        )
    }

    fun parseError(e: HttpException): ErrorResponseDTO {
        val errorBody = e.response()?.errorBody()?.string()
        return try {
            Json.decodeFromString<ErrorResponseDTO>(errorBody ?: "fuck")
        } catch (parseE: Exception) {
            getDefault(e)
        }
    }

}