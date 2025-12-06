package kr.hhplus.be.server.application.usecase.idempotency

import kr.hhplus.be.server.application.interactor.idempotency.IdempotentRequestDecision
import kr.hhplus.be.server.application.port.out.CachedResponse

interface IdempotentRequestUseCase {
    fun tryAcquire(key: String): IdempotentRequestDecision

    fun releaseOnFailure(key: String)

    fun recordSuccessIfAbsent(
        key: String,
        response: CachedResponse,
    ): CachedResponse
}
