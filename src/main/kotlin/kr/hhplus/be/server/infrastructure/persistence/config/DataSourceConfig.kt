package kr.hhplus.be.server.infrastructure.persistence.config

import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager
import javax.sql.DataSource

/**
 * 멀티 데이터 소스 설정 클래스.
 * 마스터와 슬레이브 데이터 소스를 구성하고, 동적 라우팅 데이터 소스를 설정합니다.
 * 마스터 데이터 소스는 쓰기 작업에 사용되고, 슬레이브 데이터 소스는 읽기 작업에 사용됩니다.
 * ref: https://hongchangsub.com/spring-transactional-readonly-datasource
 */
@Profile("!test")
@Configuration
class DataSourceConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.master")
    fun masterDataSourceProperties() = DataSourceProperties()

    @Bean
    fun masterDataSource(
        @Qualifier("masterDataSourceProperties") properties: DataSourceProperties,
    ): DataSource = properties.initializeDataSourceBuilder().type(HikariDataSource::class.java).build()

    @Bean
    @ConfigurationProperties("spring.datasource.slave")
    fun slaveDataSourceProperties() = DataSourceProperties()

    @Bean
    @ConfigurationProperties("spring.datasource.slave.hikari")
    fun slaveDataSource(
        @Qualifier("slaveDataSourceProperties") properties: DataSourceProperties,
    ): DataSource = properties.initializeDataSourceBuilder().type(HikariDataSource::class.java).build()

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
}

class DynamicRoutingDataSource : AbstractRoutingDataSource() {
    /**
     * 트랜잭션이 활성화되어 있는지 확인하고, 활성화된 경우 읽기 전용 트랜잭션인지 여부에 따라 데이터 소스 키를 결정합니다.
     * 읽기 전용 트랜잭션인 경우 "SLAVE" 키를
     * 그렇지 않은 경우 "MASTER" 키를 반환합니다.
     * 그리고 트랜잭션이 활성화되어 있지 않은 경우 null을 반환합니다
     */
    override fun determineCurrentLookupKey(): String? {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            val logger = KotlinLogging.logger {}
            val dataSourceType = DataSourceType.isReadOnlyTransaction(isTxReadOnly())
            logger.debug { "selected datasource = $dataSourceType" }
            return dataSourceType.value
        }

        return null
    }

    private fun isTxReadOnly(): Boolean = TransactionSynchronizationManager.isCurrentTransactionReadOnly()
}
