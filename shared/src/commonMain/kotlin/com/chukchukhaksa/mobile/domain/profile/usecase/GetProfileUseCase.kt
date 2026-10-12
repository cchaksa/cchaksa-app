package com.chukchukhaksa.mobile.domain.profile.usecase

import com.chukchukhaksa.mobile.common.model.profile.Profile
import com.chukchukhaksa.mobile.domain.common.runCatchingIgnoreCancelled
import com.chukchukhaksa.mobile.domain.profile.repository.ProfileRepository

class GetProfileUseCase(
  private val profileRepository: ProfileRepository,
) {
  suspend operator fun invoke(): Result<Profile?> = runCatchingIgnoreCancelled {
    with(profileRepository) {
      getProfileInfo()
    }
  }
}
