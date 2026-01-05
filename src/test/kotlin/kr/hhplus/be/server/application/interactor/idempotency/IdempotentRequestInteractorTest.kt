package kr.hhplus.be.server.application.interactor.idempotency

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.port.out.CachedResponse
import kr.hhplus.be.server.application.port.out.IdempotentRequestOutPort
import kr.hhplus.be.server.application.port.out.IdempotentRequestRecord
import kr.hhplus.be.server.application.port.out.IdempotentRequestState
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("IdempotentRequestInteractor 단위 테스트")
class IdempotentRequestInteractorTest {
    private val outPort: IdempotentRequestOutPort = mockk(relaxed = true)
    private val interactor = IdempotentRequestInteractor(outPort)

    @Nested
    @DisplayName("tryAcquire")
    inner class TryAcquire {
        @Test
        @DisplayName("미등록 키이면 진행 상태로 저장한다")
        fun acquiresWhenNoRecordExists() {
            every { outPort.find("key") } returns null
            every { outPort.saveInProgress("key") } returns true

            val result = interactor.tryAcquire("key")

            assertThat(result).isEqualTo(IdempotentRequestDecision.Acquired)
            verify { outPort.saveInProgress("key") }
        }

        @Test
        @DisplayName("저장 실패 시 이미 처리중으로 판정한다")
        fun returnsAlreadyInProgressWhenSaveFails() {
            every { outPort.find("key") } returns null
            every { outPort.saveInProgress("key") } returns false

            val result = interactor.tryAcquire("key")

            assertThat(result).isEqualTo(IdempotentRequestDecision.AlreadyInProgress)
        }

        @Test
        @DisplayName("완료된 응답이 있으면 Completed를 반환한다")
        fun returnsCompletedWhenCachedResponseExists() {
            val cached = CachedResponse(200, "application/json", "{}")
            every { outPort.find("key") } returns IdempotentRequestRecord(
                state = IdempotentRequestState.COMPLETED,
                response = cached,
            )

            val result = interactor.tryAcquire("key")

            assertThat(result).isInstanceOf(IdempotentRequestDecision.Completed::class.java)
            assertThat((result as IdempotentRequestDecision.Completed).response).isEqualTo(cached)
        }

        @Test
        @DisplayName("응답이 없거나 진행 중이면 AlreadyInProgress를 반환한다")
        fun returnsAlreadyInProgressWhenResponseMissing() {
            every { outPort.find("key") } returns IdempotentRequestRecord(
                state = IdempotentRequestState.IN_PROGRESS,
                response = null,
            )

            val result = interactor.tryAcquire("key")

            assertThat(result).isEqualTo(IdempotentRequestDecision.AlreadyInProgress)
        }
    }

    @Nested
    @DisplayName("recordSuccessIfAbsent")
    inner class RecordSuccessIfAbsent {
        @Test
        @DisplayName("이미 저장된 응답이 있으면 그 값을 반환한다")
        fun returnsExistingResponse() {
            val cached = CachedResponse(200, "application/json", "{}")
            every { outPort.find("key") } returns IdempotentRequestRecord(
                state = IdempotentRequestState.COMPLETED,
                response = cached,
            )

            val result = interactor.recordSuccessIfAbsent("key", CachedResponse(201, "application/json", "{\"a\":1}"))

            assertThat(result).isEqualTo(cached)
            verify(exactly = 0) { outPort.saveSuccess(any(), any()) }
        }

        @Test
        @DisplayName("응답이 없다면 저장하고 반환한다")
        fun savesWhenNoExistingResponse() {
            val response = CachedResponse(200, "application/json", "{}")
            every { outPort.find("key") } returns null

            val result = interactor.recordSuccessIfAbsent("key", response)

            assertThat(result).isEqualTo(response)
            verify { outPort.saveSuccess("key", response) }
        }
    }

    @Test
    @DisplayName("releaseOnFailure는 키를 삭제한다")
    fun releaseOnFailureDeletesKey() {
        interactor.releaseOnFailure("key")

        verify { outPort.delete("key") }
    }
}
