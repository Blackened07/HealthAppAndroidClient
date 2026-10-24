package com.blackened.healthapp.data.repository

import com.blackened.healthapp.data.network.LoginRequestDTO
import com.blackened.healthapp.data.network.NetworkClient
import com.blackened.healthapp.data.network.RegisterAccountRequestDTO
import com.blackened.healthapp.data.network.VerificationRequestDTO
import com.blackened.healthapp.domain.model.AppNetworkException
import com.blackened.healthapp.domain.model.ErrorResponseDTO
import com.blackened.healthapp.domain.model.UserSession
import com.blackened.healthapp.domain.repository.AuthRepository
import com.blackened.healthapp.utils.ErrorUtil
import kotlinx.serialization.json.Json
import retrofit2.HttpException

class AuthRepositoryImpl : AuthRepository {

    override suspend fun login(email: String, password: String): Result<UserSession> {
        return try {
            val request = LoginRequestDTO(email, password)
            val response = NetworkClient.authApi.login(request)

            if (response.success) {

                val session = UserSession(
                    token = response.message,
                    userId = response.userId,
                    firstName = response.firstName,
                    email = email
                )
                Result.success(session)
            } else {
                Result.failure(Exception("Ошибка авторизации"))
            }
        } catch (e: HttpException) {
            val parsedError = ErrorUtil.parseError(e)
            Result.failure(AppNetworkException(parsedError))
        } catch (e: Exception) {
            Result.failure(AppNetworkException(ErrorUtil.getDefault(e)))
        }
    }

    override suspend fun register(email: String, firstName: String, role: String, password: String): Result<Long> {
        return try {
            val request = RegisterAccountRequestDTO(email, password, firstName, role)
            val response = NetworkClient.authApi.register(request)
            Result.success(response.id)
        } catch (httpException: HttpException) {
            val parsedError = ErrorUtil.parseError(httpException)
            Result.failure(AppNetworkException(parsedError))
        } catch (e: Exception) {
            Result.failure(AppNetworkException(ErrorUtil.getDefault(e)))
        }
    }


    override suspend fun verifyCode(email: String, code: String): Result<String> {
        return try {
            val request = VerificationRequestDTO(email, code)
            val responseBody = NetworkClient.authApi.verify(request)
            val serverMessage = responseBody.string()
            Result.success(serverMessage)
        } catch (httpException: HttpException) {
            val parsedError = ErrorUtil.parseError(httpException)
            Result.failure(AppNetworkException(parsedError))
        }  catch (e: Exception) {
            Result.failure(AppNetworkException(ErrorUtil.getDefault(e)))
        }
    }

    override suspend fun logout(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun isTokenValid(): Boolean {
        return try {
            NetworkClient.familyApi.isNoFamily()
            true
        } catch (e: HttpException) {
            val errorBody = ErrorUtil.parseError(e)

            !(e.code() == 401 && errorBody.message.contains("Token expired"))
        } catch (e: Exception) {
            true
        }
    }
}