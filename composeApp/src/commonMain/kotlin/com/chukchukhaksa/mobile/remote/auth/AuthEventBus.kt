package com.chukchukhaksa.mobile.remote.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AuthEventBus {
    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    fun emit(event: AuthEvent = AuthEvent.TokenExpired) {
        _events.tryEmit(event)
    }
}

sealed interface AuthEvent {
    data object TokenExpired : AuthEvent
}
