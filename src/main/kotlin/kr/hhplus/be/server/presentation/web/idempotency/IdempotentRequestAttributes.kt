package kr.hhplus.be.server.presentation.web.idempotency

object IdempotentRequestAttributes {
    const val REQUEST_ATTRIBUTE_CACHE_KEY = "IDEMPOTENCY_CACHE_KEY"
    const val REQUEST_ATTRIBUTE_CACHED_RESPONSE = "IDEMPOTENCY_CACHED_RESPONSE"

    const val MEMBER_HEADER = "X-Member-Id"
}
