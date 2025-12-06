package kr.hhplus.be.server.domain.exception

class NotFoundResourceException(
    override val message: String,
    override val cause: Throwable? = null,
    val clue: Map<String, Any> = emptyMap(),
) : RuntimeException(message, cause)
