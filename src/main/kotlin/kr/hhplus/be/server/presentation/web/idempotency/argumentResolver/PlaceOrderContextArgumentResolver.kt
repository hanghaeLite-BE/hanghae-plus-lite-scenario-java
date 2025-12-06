package kr.hhplus.be.server.presentation.web.idempotency.argumentResolver

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import kr.hhplus.be.server.application.port.out.CachedResponse
import kr.hhplus.be.server.presentation.dto.order.PlaceOrderResponse
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
 * `@IdempotentContext` 어노테이션 + 타입이 `IdempotentRequestContext<PlaceOrderResponse>`인 함수 파라미터의 Argument Resolver
 * - 현재 요청에서 캐시된 응답이 존재하면 이를 `PlaceOrderResponse` 객체로 변환하여 제공
 * - 캐시된 응답이 없으면 null을 반환하여 컨트롤러 메서드에서 새로 처리할 수 있도록 함
 */
@Component
class PlaceOrderContextArgumentResolver(
    private val objectMapper: ObjectMapper,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) =
        parameter.hasParameterAnnotation(IdempotentContext::class.java) &&
            IdempotentRequestContext::class.java == parameter.parameterType &&
            (parameter.genericParameterType as ParameterizedType).actualTypeArguments.first() == PlaceOrderResponse::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): IdempotentRequestContext<PlaceOrderResponse> {
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
                    .toPlaceOrderResponse(),
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

    private fun Optional<CachedResponse>.toPlaceOrderResponse(): PlaceOrderResponse? {
        val cached = this.getOrNull() ?: return null

        if (HttpStatus.OK.value() != cached.statusCode) {
            throw IllegalStateException("Cached response status code is not 200: ${cached.statusCode}")
        }

        return objectMapper.readValue(cached.body, PlaceOrderResponse::class.java)
    }
}
