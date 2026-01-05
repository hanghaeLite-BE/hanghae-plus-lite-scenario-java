package kr.hhplus.be.server.application.enums

/**
 * 상품조회 정렬 기준
 * - REGISTER: 등록
 * - PRICE: 가격
 */
enum class ListingProductSortBy {
    REGISTER,
    PRICE,
    ;

    companion object {
        fun from(sortBy: String): ListingProductSortBy? = entries.find { it.name.equals(sortBy, ignoreCase = true) }
    }
}

/**
 * 상품조회 오름차순/내림차순
 * - ASC: 오름차순
 * - DESC: 내림차순
 */
enum class ListingProductDescending {
    ASC,
    DESC,
    ;

    companion object {
        fun from(descending: String): ListingProductDescending? = entries.find { it.name.equals(descending, ignoreCase = true) }
    }
}
