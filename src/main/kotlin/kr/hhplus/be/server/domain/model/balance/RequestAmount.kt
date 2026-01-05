package kr.hhplus.be.server.domain.model.balance

/**
 * 요청 금액을 나타내는 Value Object
 * 충전/차감 요청 시 사용되며, 0보다 큰 값만 허용합니다.
 */
data class RequestAmount(
    val value: Long,
) {
    init {
        require(value > 0) { "금액은 0원보다 커야합니다." }
    }
}
