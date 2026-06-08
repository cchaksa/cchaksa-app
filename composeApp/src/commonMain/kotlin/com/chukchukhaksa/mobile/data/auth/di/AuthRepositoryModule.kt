package com.chukchukhaksa.mobile.data.auth.di

import com.chukchukhaksa.mobile.data.auth.datasource.RemoteAuthDataSource
import com.chukchukhaksa.mobile.data.auth.repository.AuthRepositoryImpl
import com.chukchukhaksa.mobile.domain.auth.repository.AuthRepository
import com.chukchukhaksa.mobile.remote.auth.RemoteAuthDataSourceImpl
import com.chukchukhaksa.mobile.remote.di.AUTH_REFRESH_CLIENT_QUALIFIER
import org.koin.dsl.module

val authRepositoryModule = module {
    // 인증 엔드포인트(로그인·토큰 갱신)는 Auth 플러그인이 없는 클라이언트로 호출해야
    // 401 응답 시 갱신 로직이 중첩 실행(중복 auth/refresh 요청)되는 것을 막을 수 있다.
    single<RemoteAuthDataSource> {
        RemoteAuthDataSourceImpl(get(qualifier = AUTH_REFRESH_CLIENT_QUALIFIER))
    }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
}
