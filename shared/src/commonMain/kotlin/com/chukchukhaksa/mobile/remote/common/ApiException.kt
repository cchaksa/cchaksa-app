package com.chukchukhaksa.mobile.remote.common

class ApiException(
    val code: String,
    override val message: String,
) : RuntimeException(message)
