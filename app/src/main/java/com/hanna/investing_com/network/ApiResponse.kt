package com.hanna.investing_com.network

import com.hanna.investing_com.entities.ResponseStatus
import retrofit2.Response

/**
 * Common class used by API responses.
 * Based on the class provided by android architecture-components sample.
 * @param <T> the type of the response object
</T> */
@Suppress("unused") // T is used in extending classes
sealed class ApiResponse<T> {
    companion object {
        fun <T> create(error: Throwable): ApiErrorResponse<T> {
            return ApiErrorResponse(error.message ?: "unknown error")
        }

        fun <T> create(response: Response<T>): ApiResponse<T> {
            if (response.isSuccessful) {
                return when (val body = response.body()!!) {//response will always include a body
                    null -> ApiEmptyResponse()
                    is ResponseStatus -> (ApiSuccessResponse(response.body()!!).takeIf { body.status == "OK" } ?: ApiErrorResponse(body.status)) as ApiResponse<T>
                    else -> ApiSuccessResponse(body)
                }

            } else {
                val msg = response.errorBody()?.string()
                val errorMsg = if (msg.isNullOrEmpty()) response.message() else msg

                return ApiErrorResponse(errorMsg ?: "unknown error")
            }
        }
    }
}

//response types
class ApiEmptyResponse<T> : ApiResponse<T>()

data class ApiSuccessResponse<T>(val body: T) : ApiResponse<T>()

data class ApiErrorResponse<T>(val errorMessage: String) : ApiResponse<T>()