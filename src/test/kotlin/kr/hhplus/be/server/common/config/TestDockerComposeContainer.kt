package kr.hhplus.be.server.common.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import java.io.File
import java.time.Duration

@Profile("test")
@Configuration("TestDockerComposeContainer")
class TestDockerComposeContainer {
    companion object {
        private const val MYSQL_MASTER_SERVICE_NAME = "mysql-test-master"
        private const val MYSQL_SLAVE_SERVICE_NAME = "mysql-test-slave"
        private const val MYSQL_PORT = 3306
        private const val MYSQL_DATABASE_NAME = "grindelwald"
        const val MYSQL_USERNAME = "test"
        const val MYSQL_PASSWORD = "test"

        private const val REDIS_SERVICE_NAME = "redis-test"
        private const val REDIS_PORT = 6379

        private val logger = KotlinLogging.logger { }

        @JvmStatic
        @Container
        val composeContainer: DockerComposeContainer<*> =
            DockerComposeContainer(
                File("docker-compose-for-integration-test.yml"),
            ).apply {
                withExposedService(
                    MYSQL_MASTER_SERVICE_NAME,
                    MYSQL_PORT,
                    Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)),
                )
                withExposedService(
                    MYSQL_SLAVE_SERVICE_NAME,
                    MYSQL_PORT,
                    Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)),
                )
                withExposedService(
                    REDIS_SERVICE_NAME,
                    REDIS_PORT,
                    Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)),
                )
                start()
            }

        fun getMasterJdbcUrl(): String {
            val masterMappedPort = composeContainer.getServicePort(MYSQL_MASTER_SERVICE_NAME, MYSQL_PORT)
            return "jdbc:mysql://localhost:%d/$MYSQL_DATABASE_NAME".format(masterMappedPort)
        }

        fun getSlaveJdbcUrl(): String {
            val slaveMappedPort = composeContainer.getServicePort(MYSQL_SLAVE_SERVICE_NAME, MYSQL_PORT)
            return "jdbc:mysql://localhost:%d/$MYSQL_DATABASE_NAME".format(slaveMappedPort)
        }

        private fun getRedisHost(): String = composeContainer.getServiceHost(REDIS_SERVICE_NAME, REDIS_PORT)

        private fun getRedisMappedPort(): Int = composeContainer.getServicePort(REDIS_SERVICE_NAME, REDIS_PORT)

        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host", ::getRedisHost)
            registry.add("spring.data.redis.port") { getRedisMappedPort() }

            logger.info {
                "Test containers started - master=${getMasterJdbcUrl()}, slave=${getSlaveJdbcUrl()}, redis=${getRedisHost()}:${getRedisMappedPort()}"
            }
        }
    }
}
