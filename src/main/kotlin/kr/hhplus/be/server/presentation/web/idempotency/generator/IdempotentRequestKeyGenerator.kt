package kr.hhplus.be.server.presentation.web.idempotency.generator

/**
 * Contract for generating cache keys used to identify idempotent HTTP requests.
 */
interface IdempotentRequestKeyGenerator {
    fun generate(context: IdempotentRequestKeyContext): String
}
