package kr.hhplus.be.server.application.interactor.idempotency

import kr.hhplus.be.server.application.port.out.CachedResponse
import kr.hhplus.be.server.application.port.out.IdempotentRequestOutPort
import kr.hhplus.be.server.application.port.out.IdempotentRequestState
import kr.hhplus.be.server.application.usecase.idempotency.IdempotentRequestUseCase
import org.springframework.stereotype.Service

/**
 * 멱등성 요청의 상태를 관리한다.
 * 다양한 어댑터(http, gRPC, 이벤트 등)에서 공통으로 사용할 수 있도록 도메인 로직을 제공한다.
 */
@Service
class IdempotentRequestInteractor(
    private val idempotentRequestOutPort: IdempotentRequestOutPort,
) : IdempotentRequestUseCase {
    override fun tryAcquire(key: String): IdempotentRequestDecision {
        val record = idempotentRequestOutPort.find(key)
        if (record == null) {
            val acquired = idempotentRequestOutPort.saveInProgress(key)
            return if (acquired) {
                IdempotentRequestDecision.Acquired
            } else {
                IdempotentRequestDecision.AlreadyInProgress
            }
        }

        return if (record.state == IdempotentRequestState.COMPLETED && record.response != null) {
            IdempotentRequestDecision.Completed(record.response)
        } else {
            IdempotentRequestDecision.AlreadyInProgress
        }
    }

    override fun recordSuccessIfAbsent(
        key: String,
        response: CachedResponse,
    ): CachedResponse {
        val existing = idempotentRequestOutPort.find(key)?.response
        if (existing != null) {
            return existing
        }

        idempotentRequestOutPort.saveSuccess(key, response)
        return response
    }

    override fun releaseOnFailure(key: String) {
        idempotentRequestOutPort.delete(key)
    }
}

sealed class IdempotentRequestDecision {
    data object Acquired : IdempotentRequestDecision()

    data object AlreadyInProgress : IdempotentRequestDecision()

    data class Completed(
        val response: CachedResponse,
    ) : IdempotentRequestDecision()
}
