package kr.hhplus.be.server.infrastructure.persistence.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Profile("!test")
@Configuration
@EnableJpaRepositories(
    basePackages = ["kr.hhplus.be.server.infrastructure.persistence"],
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager",
)
class JpaConfig {
    @Bean("transactionManager")
    fun transactionManager(): PlatformTransactionManager = JpaTransactionManager()

    @Primary
    @Bean("entityManagerFactory")
    fun entityManager(dataSource: DataSource): LocalContainerEntityManagerFactoryBean =
        LocalContainerEntityManagerFactoryBean().apply {
            this.dataSource = dataSource
            this.setPackagesToScan("kr.hhplus.be.server.infrastructure.persistence")
            this.jpaVendorAdapter =
                org.springframework.orm.jpa.vendor
                    .HibernateJpaVendorAdapter()
            this.afterPropertiesSet()
        }
}
