package kr.hhplus.be.server.common.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class NoOpEventPublisherConfig {
    @Bean
    @Primary
    fun noOpApplicationEventPublisher(): ApplicationEventPublisher =
        ApplicationEventPublisher { _ ->
            println("NoOpEventPublisher: Event publishing is disabled in tests.")
        }
}
