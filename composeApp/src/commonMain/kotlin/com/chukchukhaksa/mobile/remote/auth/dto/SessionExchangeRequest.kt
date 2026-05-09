package com.chukchukhaksa.mobile.remote.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionExchangeRequest(
  val accessToken: String,
  val refreshToken: String,
)
