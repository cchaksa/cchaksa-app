package com.chukchukhaksa.mobile.domain.user.usecase

import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.remote.user.UserApi

class GetPortalLinkStatusUseCase(
  private val userApi: UserApi,
) {
  suspend operator fun invoke(): Result<Boolean> = runCatchingIgnoreCancelled {
    userApi.getMe().isPortalLinked
  }
}
