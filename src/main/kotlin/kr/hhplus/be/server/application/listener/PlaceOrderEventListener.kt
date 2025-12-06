package kr.hhplus.be.server.application.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.hhplus.be.server.application.port.out.ExternalServiceOutput
import kr.hhplus.be.server.application.vo.PlaceOrderResultVO
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PlaceOrderEventListener(
    private val externalServiceOutput: ExternalServiceOutput,
) {
    private val logger = KotlinLogging.logger {}

    // 트랜잭션이 성공적으로 커밋된 후에 이벤트를 처리
    @TransactionalEventListener
    fun handlePlaceOrderEvent(placeOrderResult: PlaceOrderResultVO) {
        logger.debug { "주문완료 이벤트 핸들링: $placeOrderResult" }
        externalServiceOutput.call(placeOrderResult)
    }
}
