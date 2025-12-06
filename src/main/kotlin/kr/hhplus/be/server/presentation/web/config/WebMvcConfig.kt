package kr.hhplus.be.server.presentation.web.config

import kr.hhplus.be.server.infrastructure.config.IdempotentRequestProperties
import kr.hhplus.be.server.presentation.web.idempotency.IdempotentRequestInterceptor
import kr.hhplus.be.server.presentation.web.idempotency.argumentResolver.CouponIssuanceContextArgumentResolver
import kr.hhplus.be.server.presentation.web.idempotency.argumentResolver.PlaceOrderContextArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val idempotentRequestInterceptor: IdempotentRequestInterceptor,
    private val placeOrderContextArgumentResolver: PlaceOrderContextArgumentResolver,
    private val couponIssuanceContextArgumentResolver: CouponIssuanceContextArgumentResolver,
    private val idempotentRequestProperties: IdempotentRequestProperties,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
            .addInterceptor(idempotentRequestInterceptor)
            .addPathPatterns(*idempotentRequestProperties.paths.toTypedArray())
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(placeOrderContextArgumentResolver)
        resolvers.add(couponIssuanceContextArgumentResolver)
    }
}
