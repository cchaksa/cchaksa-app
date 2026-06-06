package com.chukchukhaksa.mobile.remote.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionExchangeSuccessDto(
  val ok: Boolean,
)

@Serializable
data class SessionExchangeErrorDto(
  val error: String,
)
