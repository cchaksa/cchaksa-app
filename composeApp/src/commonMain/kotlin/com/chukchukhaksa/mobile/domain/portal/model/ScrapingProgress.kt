package com.chukchukhaksa.mobile.domain.portal.model

sealed interface ScrapingProgress {
    val jobId: String

    data class Accepted(
        override val jobId: String,
    ) : ScrapingProgress

    data class InProgress(
        override val jobId: String,
        val status: String,
    ) : ScrapingProgress

    data class Completed(
        override val jobId: String,
        val result: ScrapingResult,
    ) : ScrapingProgress
}
