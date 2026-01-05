package kr.hhplus.be.server.presentation.web.idempotency.generator

data class IdempotentRequestKeyContext(
    val httpMethod: String,
    val requestUri: String,
    val memberId: String?,
    val idempotencyKey: String,
)
