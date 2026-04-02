package com.chukchukhaksa.mobile.data.profile.di

import com.chukchukhaksa.mobile.data.profile.datasource.RemoteProfileDataSource
import com.chukchukhaksa.mobile.data.profile.repository.ProfileRepositoryImpl
import com.chukchukhaksa.mobile.domain.profile.repository.ProfileRepository
import com.chukchukhaksa.mobile.remote.profile.RemoteProfileDataSourceImpl
import org.koin.dsl.module

val ProfileRepositoryModule = module {
    single<RemoteProfileDataSource> { RemoteProfileDataSourceImpl() }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
}
