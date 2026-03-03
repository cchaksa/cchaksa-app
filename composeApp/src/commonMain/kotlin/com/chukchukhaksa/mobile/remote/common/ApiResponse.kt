package com.chukchukhaksa.mobile.remote.common

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null,
)

@Serializable
data class ApiError(
    val code: String,
    val message: String,
)

fun <T> ApiResponse<T>.getDataOrThrow(): T {
    return data ?: throw ApiException(
        code = error?.code.orEmpty(),
        message = error?.message ?: "알 수 없는 에러가 발생했어요.",
    )
}
