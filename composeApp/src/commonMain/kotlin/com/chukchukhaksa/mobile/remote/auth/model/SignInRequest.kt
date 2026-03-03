package com.chukchukhaksa.mobile.remote.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignInRequest(
    @SerialName("id_token") val idToken: String,
    val nonce: String,
)
