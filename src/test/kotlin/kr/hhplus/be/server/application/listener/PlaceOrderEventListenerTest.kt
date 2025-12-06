package kr.hhplus.be.server.application.listener

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kr.hhplus.be.server.application.port.out.ExternalServiceOutput
import kr.hhplus.be.server.application.vo.PlaceOrderItemVO
import kr.hhplus.be.server.application.vo.PlaceOrderResultVO
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.util.AopTestUtils
import java.util.concurrent.Executor

@SpringJUnitConfig(PlaceOrderEventListenerTest.TestConfig::class)
class PlaceOrderEventListenerTest {
    @Autowired
    private lateinit var placeOrderEventListener: PlaceOrderEventListener

    @Autowired
    private lateinit var externalServiceOutput: ExternalServiceOutput

    private val listenerSpy: PlaceOrderEventListener
        get() = AopTestUtils.getTargetObject(placeOrderEventListener)

    @AfterEach
    fun tearDown() {
        clearMocks(listenerSpy, externalServiceOutput)
    }

    private val placeOrderResult =
        PlaceOrderResultVO(
            orderId = 1L,
            paymentMethod = "POINT",
            paymentChargeAmount = 1000L,
            paymentDiscountAmount = 0L,
            paymentTotalAmount = 1000L,
            orderItems = listOf(PlaceOrderItemVO(quantity = 1, price = 1000, productSummaryId = 1L)),
        )

    @Test
    @DisplayName("재시도 없이 주문완료 이벤트를 처리한다")
    fun `handlePlaceOrderEvent succeeds without retry`() {
        every { externalServiceOutput.call(placeOrderResult) } returns Unit

        placeOrderEventListener.handlePlaceOrderEvent(placeOrderResult)

        verify(exactly = 1) { externalServiceOutput.call(placeOrderResult) }
        verify(exactly = 1) { listenerSpy.handlePlaceOrderEvent(placeOrderResult) }
    }

    @Test
    @DisplayName("재시도 중 성공하면 재시도를 더 이상 하지 않는다")
    fun `handlePlaceOrderEvent retries up to success`() {
        every { externalServiceOutput.call(placeOrderResult) } throws RuntimeException("first") andThenThrows
            RuntimeException("second") andThen Unit

        placeOrderEventListener.handlePlaceOrderEvent(placeOrderResult)

        verify(exactly = 3) { externalServiceOutput.call(placeOrderResult) }
        verify(exactly = 3) { listenerSpy.handlePlaceOrderEvent(placeOrderResult) }
    }

    @Test
    @DisplayName("모든 재시도 후 실패하면 복구 로직이 실행된다")
    fun `handlePlaceOrderEvent retries up to recover`() {
        every { externalServiceOutput.call(placeOrderResult) } throws RuntimeException("failure")

        placeOrderEventListener.handlePlaceOrderEvent(placeOrderResult)

        verify(exactly = 1) { listenerSpy.recoverPlaceOrderEvent(any(), placeOrderResult) }
        verify(exactly = 3) { externalServiceOutput.call(placeOrderResult) }
        verify(exactly = 3) { listenerSpy.handlePlaceOrderEvent(placeOrderResult) }
    }

    @TestConfiguration
    @EnableRetry(proxyTargetClass = true)
    @EnableAsync(proxyTargetClass = true)
    class TestConfig : AsyncConfigurer {
        @Bean
        fun externalServiceOutput(): ExternalServiceOutput = io.mockk.mockk(relaxed = true)

        @Bean
        fun placeOrderEventListener(externalServiceOutput: ExternalServiceOutput): PlaceOrderEventListener =
            spyk(PlaceOrderEventListener(externalServiceOutput))

        override fun getAsyncExecutor(): Executor = SyncTaskExecutor()
    }
}
