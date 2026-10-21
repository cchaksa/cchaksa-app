package com.chukchukhaksa.mobile.data.auth.datasource

/**
 * HTTP 클라이언트가 메모리에 들고 있는 인증 토큰.
 *
 * 클라이언트는 [LocalAuthDataSource]에서 한 번 읽은 토큰을 프로세스가 끝날 때까지 다시 읽지 않는다.
 * 로그인·세션 복원·탈퇴처럼 클라이언트 밖에서 저장된 토큰을 바꾸면 [clear]를 불러야 다음 요청이 바뀐 토큰을 쓴다.
 */
interface AuthTokenCache {
    fun clear()
}
