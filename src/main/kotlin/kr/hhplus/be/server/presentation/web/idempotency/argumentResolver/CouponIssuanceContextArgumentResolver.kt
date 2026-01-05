package kr.hhplus.be.server.presentation.web.idempotency.argumentResolver

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import kr.hhplus.be.server.application.port.out.CachedResponse
import kr.hhplus.be.server.presentation.dto.coupon.CouponIssuanceResponse
import kr.hhplus.be.server.presentation.web.idempotency.IdempotentContext
import kr.hhplus.be.server.presentation.web.idempotency.IdempotentRequestAttributes
import kr.hhplus.be.server.presentation.web.idempotency.IdempotentRequestContext
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.lang.reflect.ParameterizedType
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

/**
 * `@IdempotentContext` + `IdempotentRequestContext<CouponIssuanceResponse>` 파라미터 지원 Argument Resolver
 * - 캐시된 응답이 있으면 `CouponIssuanceResponse`로 역직렬화해 반환
 * - 없으면 null을 전달하여 컨트롤러가 신규 처리를 수행하도록 한다
 */
@Component
class CouponIssuanceContextArgumentResolver(
    private val objectMapper: ObjectMapper,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) =
        parameter.hasParameterAnnotation(IdempotentContext::class.java) &&
            IdempotentRequestContext::class.java == parameter.parameterType &&
            (parameter.genericParameterType as ParameterizedType).actualTypeArguments.first() ==
            CouponIssuanceResponse::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): IdempotentRequestContext<CouponIssuanceResponse> {
        val request =
            webRequest.getNativeRequest(HttpServletRequest::class.java)
                ?: throw IllegalStateException("HttpServletRequest is required when resolving @IdempotentContext")

        val cacheKey =
            request.getAttribute(IdempotentRequestAttributes.REQUEST_ATTRIBUTE_CACHE_KEY) as? String
                ?: throw IllegalStateException("Idempotent request cache key is missing on the current request")

        return IdempotentRequestContext(
            cacheKey = cacheKey,
            cachedResponse =
                getCachedResponse(request)
                    .toCouponIssuanceResponse(),
        )
    }

    private fun getCachedResponse(request: HttpServletRequest): Optional<CachedResponse> {
        val cachedResponse = request.getAttribute(IdempotentRequestAttributes.REQUEST_ATTRIBUTE_CACHED_RESPONSE)
        return if (cachedResponse == null) {
            Optional.empty()
        } else {
            Optional.of(cachedResponse as CachedResponse)
        }
    }

    private fun Optional<CachedResponse>.toCouponIssuanceResponse(): CouponIssuanceResponse? {
        val cached = this.getOrNull() ?: return null

        if (HttpStatus.OK.value() != cached.statusCode) {
            throw IllegalStateException("Cached response status code is not 200: ${cached.statusCode}")
        }

        return objectMapper.readValue(cached.body, CouponIssuanceResponse::class.java)
    }
}
