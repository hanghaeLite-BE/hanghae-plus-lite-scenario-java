package kr.hhplus.be.server.common.config

import com.zaxxer.hikari.HikariDataSource
import kr.hhplus.be.server.infrastructure.persistence.config.DataSourceType
import kr.hhplus.be.server.infrastructure.persistence.config.DynamicRoutingDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import javax.sql.DataSource

@Profile("test")
@Configuration
class TestDataSourceConfig {
    @Bean
    @DependsOn("TestDockerComposeContainer")
    fun masterDataSource(): HikariDataSource =
        DataSourceBuilder
            .create()
            .type(HikariDataSource::class.java)
            .url(TestDockerComposeContainer.getMasterJdbcUrl())
            .username(TestDockerComposeContainer.MYSQL_USERNAME)
            .password(TestDockerComposeContainer.MYSQL_PASSWORD)
            .build()

    @Bean
    @DependsOn("TestDockerComposeContainer")
    fun slaveDataSource(): HikariDataSource =
        DataSourceBuilder
            .create()
            .type(HikariDataSource::class.java)
            .url(TestDockerComposeContainer.getSlaveJdbcUrl())
            .username(TestDockerComposeContainer.MYSQL_USERNAME)
            .password(TestDockerComposeContainer.MYSQL_PASSWORD)
            .build()

    @Bean
    fun routingDataSource(
        @Qualifier("masterDataSource") masterDataSource: DataSource,
        @Qualifier("slaveDataSource") slaveDataSource: DataSource,
    ): DataSource {
        val dynamicRoutingDataSource = DynamicRoutingDataSource()
        val targetDataSources: Map<Any, Any> =
            mapOf(
                DataSourceType.MASTER to masterDataSource,
                DataSourceType.SLAVE to slaveDataSource,
            )
        dynamicRoutingDataSource.setTargetDataSources(targetDataSources)
        dynamicRoutingDataSource.setDefaultTargetDataSource(masterDataSource)

        return dynamicRoutingDataSource
    }

    @DependsOn("routingDataSource")
    @Primary
    @Bean
    fun dataSource(
        @Qualifier("routingDataSource") dataSource: DataSource,
    ): DataSource = LazyConnectionDataSourceProxy(dataSource)

    @Bean
    @DependsOn("TestDockerComposeContainer")
    fun redisConnectionFactory(): RedisConnectionFactory =
        LettuceConnectionFactory(
            TestDockerComposeContainer.getRedisHost(),
            TestDockerComposeContainer.getRedisMappedPort(),
        )
}
