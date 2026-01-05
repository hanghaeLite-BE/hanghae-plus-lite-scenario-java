package kr.hhplus.be.server.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("app.idempotency")
data class IdempotentRequestProperties(
    val header: String,
    val ttl: Duration,
    val paths: List<String>,
)
