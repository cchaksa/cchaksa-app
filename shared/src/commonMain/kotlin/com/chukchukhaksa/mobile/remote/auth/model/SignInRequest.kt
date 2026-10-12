package com.chukchukhaksa.mobile.remote.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignInRequest(
    val provider: String,
    @SerialName("id_token") val idToken: String,
    val nonce: String,
)
