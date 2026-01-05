package kr.hhplus.be.server.infrastructure.persistence.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.*

@EnableJpaAuditing
@Configuration
class AuditorAwareConfig {
    @Bean
    fun auditorProvider(): AuditorAware<String> = AuditorAware { Optional.of("system") }
}
