package kr.hhplus.be.server.utils

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TransactionUtils {
    @Transactional
    fun <T> onPrimaryTransaction(doSomething: () -> T) = doSomething()
}
