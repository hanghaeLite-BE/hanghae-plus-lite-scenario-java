package kr.hhplus.be.server.presentation.web.idempotency

/**
 * @param cacheKey The key used to store/retrieve the cached response.
 * @param cachedResponse The cached response if it exists, otherwise null.
 */
data class IdempotentRequestContext<T>(
    val cacheKey: String,
    val cachedResponse: T?,
) {
    fun getResponseOrElse(defaultValue: () -> T): T = cachedResponse ?: defaultValue()
}
