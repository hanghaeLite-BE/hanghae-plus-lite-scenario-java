// package kr.hhplus.be.server.common.config
//
// import jakarta.annotation.PreDestroy
// import org.springframework.boot.test.context.TestComponent
// import org.springframework.boot.test.util.TestPropertyValues
// import org.springframework.context.ApplicationContextInitializer
// import org.springframework.context.ConfigurableApplicationContext
// import org.springframework.test.context.ActiveProfiles
// import org.springframework.test.context.DynamicPropertyRegistry
// import org.springframework.test.context.DynamicPropertySource
// import org.testcontainers.containers.MySQLContainer
// import org.testcontainers.junit.jupiter.Container
//
// @ActiveProfiles("test")
// @TestComponent("sharedTestContainers")
// class SharedTestContainers {
//    @PreDestroy
//    fun cleanUp() {
//        masterContainer.stop()
//        slaveContainer.stop()
//    }
//
//    @DynamicPropertySource
//    fun overrideProps(registry: DynamicPropertyRegistry) {
//        registry.add("spring.datasource.master.url", masterContainer::getJdbcUrl)
//        registry.add("spring.datasource.master.username", masterContainer::getUsername)
//        registry.add("spring.datasource.master.password", masterContainer::getPassword)
//        registry.add("spring.datasource.slave.url", slaveContainer::getJdbcUrl)
//        registry.add("spring.datasource.slave.username", slaveContainer::getUsername)
//        registry.add("spring.datasource.slave.password", slaveContainer::getPassword)
//    }
//
//    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
//        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
//            val masterContainerUrl =
//                "jdbc:tc:mysql://${masterContainer.host}:${masterContainer.firstMappedPort}" +
//                    "/$DATABASE_NAME?characterEncoding=UTF-8&serverTimezone=UTC"
//            val slaveContainerUrl =
//                "jdbc:tc:mysql://${slaveContainer.host}:${slaveContainer.firstMappedPort}" +
//                    "/$DATABASE_NAME?characterEncoding=UTF-8&serverTimezone=UTC"
//
//            TestPropertyValues
//                .of(
//                    "spring.datasource.master.url=$masterContainerUrl",
//                    "spring.datasource.master.username=$USERNAME",
//                    "spring.datasource.master.password=$PASSWORD",
//                    "spring.datasource.slave.url=$slaveContainerUrl",
//                    "spring.datasource.slave.username=$USERNAME",
//                    "spring.datasource.slave.password=$PASSWORD",
//                ).applyTo(configurableApplicationContext.environment)
//        }
//    }
//
//    companion object {
//        const val IMAGE_NAME: String = "mysql:8.0.36"
//        const val DATABASE_NAME: String = "grindelwald"
//        const val USERNAME: String = "root"
//        const val PASSWORD: String = "test"
//
//        @Container
//        @JvmStatic
//        val masterContainer: MySQLContainer<*> =
//            MySQLContainer(IMAGE_NAME)
//                .apply {
//                    withDatabaseName(DATABASE_NAME)
//                    withUsername(USERNAME)
//                    withPassword(PASSWORD)
//                    withInitScripts("sql/schema.sql", "sql/default-member.sql")
//                    start()
//                }
//
//        @Container
//        @JvmStatic
//        val slaveContainer: MySQLContainer<*> =
//            MySQLContainer(IMAGE_NAME)
//                .apply {
//                    withDatabaseName(DATABASE_NAME)
//                    withUsername(USERNAME)
//                    withPassword(PASSWORD)
//                    withInitScripts("sql/schema.sql", "sql/default-member.sql")
//                    start()
//                }
//    }
// }
