package kr.hhplus.be.server.application.port.out

data class IdempotentRequestRecord(
    val state: IdempotentRequestState,
    val response: CachedResponse?,
)

data class CachedResponse(
    val statusCode: Int,
    val contentType: String,
    val body: String,
)

enum class IdempotentRequestState {
    IN_PROGRESS,
    COMPLETED,
}

interface IdempotentRequestOutPort {
    fun find(key: String): IdempotentRequestRecord?

    fun saveInProgress(key: String): Boolean

    fun saveSuccess(
        key: String,
        response: CachedResponse,
    )

    fun delete(key: String)
}
