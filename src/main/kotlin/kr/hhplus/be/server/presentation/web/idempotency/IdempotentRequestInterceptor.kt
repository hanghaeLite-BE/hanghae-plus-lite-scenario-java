package kr.hhplus.be.server.presentation.web.idempotency

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.hhplus.be.server.application.interactor.idempotency.IdempotentRequestDecision
import kr.hhplus.be.server.application.usecase.idempotency.IdempotentRequestUseCase
import kr.hhplus.be.server.infrastructure.config.IdempotentRequestProperties
import kr.hhplus.be.server.presentation.web.exception.ErrorResponse
import kr.hhplus.be.server.presentation.web.idempotency.generator.IdempotentRequestKeyContext
import kr.hhplus.be.server.presentation.web.idempotency.generator.IdempotentRequestKeyGenerator
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import kotlin.text.Charsets

/**
 * 멱등성 요청을 처리하기 위한 인터셉터
 * - POST 요청에 대해서만 동작
 * - 요청 헤더에서 Idempotency-Key를 추출하여 캐시 키 생성
 * - 이미 처리 중이거나 완료된 요청이 있는지 확인
 *   - 처리 중인 요청이 있으면 409 Conflict 응답 반환
 *   - 완료된 요청이 있으면 캐시된 응답을 제공하기 위해 요청 속성에 저장
 *   - 새로운 요청이면 진행 상태로 저장하고 요청 속성에 캐시 키 저장
 * - 요청 처리 후, 응답 상태가 200 OK가 아니면 진행 상태 기록 삭제
 */
@Component
class IdempotentRequestInterceptor(
    private val idempotentRequestUseCase: IdempotentRequestUseCase,
    private val idempotentRequestProperties: IdempotentRequestProperties,
    private val keyGenerator: IdempotentRequestKeyGenerator,
    private val objectMapper: ObjectMapper,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (!request.isPostMethod()) {
            return true
        }

        val idempotencyKey = request.getHeader(idempotentRequestProperties.header)
        if (idempotencyKey.isNullOrBlank()) {
            response.writeError(HttpStatus.BAD_REQUEST, "Idempotency-Key 헤더값이 필요합니다.")
            return false
        }

        val memberId = request.getHeader(IdempotentRequestAttributes.MEMBER_HEADER)
        val cacheKey =
            keyGenerator.generate(
                IdempotentRequestKeyContext(
                    httpMethod = request.method,
                    requestUri = request.requestURI,
                    memberId = memberId,
                    idempotencyKey = idempotencyKey,
                ),
            )

        return when (val decision = idempotentRequestUseCase.tryAcquire(cacheKey)) {
            IdempotentRequestDecision.Acquired -> {
                request.setAttribute(IdempotentRequestAttributes.REQUEST_ATTRIBUTE_CACHE_KEY, cacheKey)
                true
            }

            IdempotentRequestDecision.AlreadyInProgress -> {
                response.writeError(HttpStatus.CONFLICT, "중복된 요청이 이미 처리중입니다.")
                false
            }

            is IdempotentRequestDecision.Completed -> {
                request.setAttribute(IdempotentRequestAttributes.REQUEST_ATTRIBUTE_CACHED_RESPONSE, decision.response)
                request.setAttribute(IdempotentRequestAttributes.REQUEST_ATTRIBUTE_CACHE_KEY, cacheKey)
                true
            }
        }
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        val cacheKey = request.getAttribute(IdempotentRequestAttributes.REQUEST_ATTRIBUTE_CACHE_KEY) as? String ?: return

        if (!response.isOK()) {
            idempotentRequestUseCase.releaseOnFailure(cacheKey)
        }
    }

    private fun HttpServletRequest.isPostMethod(): Boolean = this.method.equals("POST", ignoreCase = true)

    private fun HttpServletResponse.isOK(): Boolean = this.status == HttpStatus.OK.value()

    private fun HttpServletResponse.writeError(
        status: HttpStatus,
        message: String,
    ) {
        this.status = status.value()
        this.contentType = MediaType.APPLICATION_JSON_VALUE
        this.characterEncoding = Charsets.UTF_8.name()
        this.writer.use { writer ->
            writer.write(objectMapper.writeValueAsString(ErrorResponse(message)))
            writer.flush()
        }
    }
}
