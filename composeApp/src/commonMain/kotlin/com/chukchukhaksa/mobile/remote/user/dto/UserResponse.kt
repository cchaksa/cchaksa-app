package com.chukchukhaksa.mobile.remote.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserResponseData(
  val isPortalLinked: Boolean,
)
