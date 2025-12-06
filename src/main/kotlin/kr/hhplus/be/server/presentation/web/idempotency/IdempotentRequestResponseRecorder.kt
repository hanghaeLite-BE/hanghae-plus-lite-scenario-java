package kr.hhplus.be.server.presentation.web.idempotency

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.application.port.out.CachedResponse
import kr.hhplus.be.server.application.usecase.idempotency.IdempotentRequestUseCase
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

/**
 * 멱등성 요청의 결과를 기록하는 역할을 합니다.
 */
@Component
class IdempotentRequestResponseRecorder(
    private val idempotentRequestUseCase: IdempotentRequestUseCase,
    private val objectMapper: ObjectMapper,
) {
    fun <T : Any> recordSuccess(
        cacheKey: String,
        responseBody: T,
        statusCode: Int,
        responseType: Class<T>,
    ): T {
        val serialized = objectMapper.writeValueAsString(responseBody)
        val cached =
            idempotentRequestUseCase.recordSuccessIfAbsent(
                key = cacheKey,
                response =
                    CachedResponse(
                        statusCode = statusCode,
                        contentType = MediaType.APPLICATION_JSON_VALUE,
                        body = serialized,
                    ),
            )

        return if (cached.body == serialized) {
            responseBody
        } else {
            objectMapper.readValue(cached.body, responseType)
        }
    }
}
