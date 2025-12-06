package kr.hhplus.be.server.infrastructure.idempotency

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.application.port.out.CachedResponse
import kr.hhplus.be.server.application.port.out.IdempotentRequestOutPort
import kr.hhplus.be.server.application.port.out.IdempotentRequestRecord
import kr.hhplus.be.server.application.port.out.IdempotentRequestState
import kr.hhplus.be.server.infrastructure.config.IdempotentRequestProperties
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository

@Repository
class RedisIdempotentRequestRepository(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val properties: IdempotentRequestProperties,
) : IdempotentRequestOutPort {
    override fun find(key: String): IdempotentRequestRecord? {
        val raw = redisTemplate.opsForValue().get(key) ?: return null
        return try {
            objectMapper.readValue(raw, SerializedRecord::class.java).toDomain()
        } catch (_: Exception) {
            null
        }
    }

    override fun saveInProgress(key: String): Boolean {
        val record = SerializedRecord(state = SerializedState.IN_PROGRESS)
        return redisTemplate.opsForValue().setIfAbsent(key, objectMapper.writeValueAsString(record), properties.ttl) ?: false
    }

    override fun saveSuccess(
        key: String,
        response: CachedResponse,
    ) {
        val record =
            SerializedRecord(
                state = SerializedState.COMPLETED,
                response =
                    SerializedResponse(
                        statusCode = response.statusCode,
                        contentType = response.contentType,
                        body = response.body,
                    ),
            )
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(record), properties.ttl)
    }

    override fun delete(key: String) {
        redisTemplate.delete(key)
    }

    private data class SerializedRecord(
        val state: SerializedState,
        val response: SerializedResponse? = null,
    ) {
        fun toDomain(): IdempotentRequestRecord =
            IdempotentRequestRecord(
                state = state.toDomain(),
                response = response?.toDomain(),
            )
    }

    private data class SerializedResponse(
        val statusCode: Int,
        val contentType: String,
        val body: String,
    ) {
        fun toDomain(): CachedResponse =
            CachedResponse(
                statusCode = statusCode,
                contentType = contentType,
                body = body,
            )
    }

    private enum class SerializedState {
        IN_PROGRESS,
        COMPLETED,
        ;

        fun toDomain(): IdempotentRequestState = IdempotentRequestState.valueOf(name)
    }
}
