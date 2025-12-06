package kr.hhplus.be.server.presentation.web.idempotency.generator

import org.springframework.stereotype.Component

/**
 * Generates a cache key including the member segment when a member id is present.
 * E.g. `idempotent:POST:/api/v1/orders:member:123:abc-456`
 */
@Component
class IdempotentRequestKeyWithMemberSegmentGenerator : IdempotentRequestKeyGenerator {
    override fun generate(context: IdempotentRequestKeyContext): String {
        val memberSegment = memberSegment(memberId = context.memberId)
        val normalizedMethod = context.httpMethod.uppercase()
        return "idempotent:$normalizedMethod:${context.requestUri}$memberSegment:${context.idempotencyKey}"
    }

    private fun memberSegment(memberId: String?) =
        if (memberId.isNullOrBlank()) {
            ""
        } else {
            ":member:$memberId"
        }
}
