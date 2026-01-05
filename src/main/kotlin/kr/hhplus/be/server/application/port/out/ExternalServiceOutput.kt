package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.application.vo.PlaceOrderResultVO

/**
 * Port for calling external services.
 */
interface ExternalServiceOutput {
    fun call(dto: PlaceOrderResultVO)
}
