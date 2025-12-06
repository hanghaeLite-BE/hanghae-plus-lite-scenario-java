package kr.hhplus.be.server.infrastructure.persistence.config

enum class DataSourceType(
    val value: String,
) {
    MASTER("master"),
    SLAVE("slave"), ;

    companion object {
        fun isReadOnlyTransaction(txReadOnly: Boolean): DataSourceType =
            if (txReadOnly) {
                SLAVE
            } else {
                MASTER
            }
    }
}
