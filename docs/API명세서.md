# API 명세서

## 잔액 관리 API

### 내 잔액 조회

<details>
 <summary><code>GET</code> <code><b>/api/v1/balances/me</b></code> <code>(현재 사용자의 잔액을 조회합니다)</code></summary>

##### Headers

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | X-Member-Id |  required | long   | 회원 ID  |

##### Parameters

> None

##### Responses

> | http code     | content-type                      | response                                    |
> |---------------|-----------------------------------|---------------------------------------------|
> | `200`         | `application/json`                | `{ "balanceId": 1, "availableAmount": 10000 }`                        |
> | `400`         | `application/json`                | `{ "message": "X-Member-Id 헤더가 필요합니다." }` |
> | `404`         | `application/json`                | `{ "message": "잔고정보를 찾을 수 없습니다." }` |

##### Example cURL

> ```bash
>  curl -X GET -H "X-Member-Id: 1" http://localhost:8080/api/v1/balances/me
> ```

</details>

------------------------------------------------------------------------------------------

### 내 잔액 충전

<details>
 <summary><code>PUT</code> <code><b>/api/v1/balances/me/{memberBalanceId}/recharge</b></code> <code>(사용자의 잔액을 충전합니다)</code></summary>

##### Headers

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | X-Member-Id |  required | long   | 회원 ID  |

##### Parameters

> | name              |  type     | data type      | description                         |
> |-------------------|-----------|----------------|-------------------------------------|
> | `memberBalanceId` |  required | long           | 회원 잔액 ID                        |

##### Request Body

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | chargeAmount |  required | long   | 충전할 금액  |

##### Request Body Example

> ```json
> {
>   "chargeAmount": 10000
> }
> ```

##### Responses

> | http code     | content-type                      | response                                    |
> |---------------|-----------------------------------|---------------------------------------------|
> | `200`         | `application/json`                | `{ "balanceId": 1, "availableAmount": 15000 }`                        |
> | `400`         | `application/json`                | `{ "message": "X-Member-Id 헤더가 필요합니다." }`<br>`{ "message": "충전 금액은 0원보다 커야합니다." }`  |
> | `404`         | `application/json`                | `{ "message": "잔고정보를 찾을 수 없습니다." }` |

##### Example cURL

> ```bash
>  curl -X PUT -H "Content-Type: application/json" -H "X-Member-Id: 1" \
>       --data '{"chargeAmount": 10000}' \
>       http://localhost:8080/api/v1/balances/me/1/recharge
> ```

</details>

------------------------------------------------------------------------------------------

## 상품 API

### 상품 목록 조회

<details>
 <summary><code>GET</code> <code><b>/api/v1/products</b></code> <code>(상품 목록을 페이징하여 조회합니다)</code></summary>

##### Parameters

> | name      |  type     | data type      | description                         |
> |-----------|-----------|----------------|-------------------------------------|
> | `page`    |  optional | int            | 페이지 번호 (기본값: 0)              |
> | `size`    |  optional | int            | 페이지 크기 (기본값: 10)             |
> | `sortBy`  |  optional | string         | 정렬 기준 (기본값: register)         |
> | `descending` | optional | string        | 내림차순 여부 (기본값: desc)         |

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `{ "rows": 10, "page": 0, "products": [ { "id": 1, "name": "상품명", "price": 10000, "stockQuantity": 100 } ] }` |
> | `400`         | `application/json`                | `{ "message": "잘못된 파라미터입니다." }`                   |

##### Example cURL

> ```bash
>  curl -X GET "http://localhost:8080/api/v1/products?page=0&size=10"
> ```

</details>

------------------------------------------------------------------------------------------

### 인기 상품 조회

<details>
 <summary><code>GET</code> <code><b>/api/v1/products/top-selling</b></code> <code>(최근 n일간 가장 많이 팔린 상위 m개 상품을 조회합니다)</code></summary>

##### Parameters

> | name      |  type     | data type      | description                         |
> |-----------|-----------|----------------|-------------------------------------|
> | `nDay`    |  optional | int            | 최근 n일 (기본값: 3)                |
> | `mProduct`|  optional | int            | 상위 m개 (기본값: 5)                |

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `{ "count": 5, "products": [ { "id": 1, "name": "인기상품", "price": 20000, "stockQuantity": 50, "totalOrderQuantity": 10 } ] }` |
> | `400`         | `application/json`                | `{ "message": "조회 기간 및 갯수는 0보다 커야합니다." }` |

##### Example cURL

> ```bash
>  curl -X GET "http://localhost:8080/api/v1/products/top-selling?nDay=3&mProduct=5"
> ```

</details>

------------------------------------------------------------------------------------------

## 쿠폰 API

### 쿠폰 발급

<details>
 <summary><code>POST</code> <code><b>/api/v1/coupons/issuance</b></code> <code>(쿠폰을 발급받습니다)</code></summary>

##### Headers

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | X-Member-Id |  required | long   | 회원 ID  |

##### Request Body

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | couponSummaryId |  required | long   | 쿠폰 요약 ID  |

##### Request Body Example

> ```json
> {
>   "couponSummaryId": 1
> }
> ```

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `{ "couponId": 1, "expiredAt": "2025-12-31T23:59:59" }` |
> | `409`         | `application/json`                | `{ "message": "쿠폰발급이 가능한 기간이 아닙니다." }`<br>`{ "message": "쿠폰발급수량이 부족합니다." }`<br>`{ "message": "중복발급이 제한된 쿠폰입니다." }` |

##### Example cURL

> ```bash
>  curl -X POST -H "X-Member-Id: 1" -H "Content-Type: application/json" \
>       --data '{"couponSummaryId": 1}' \
>       http://localhost:8080/api/v1/coupons/issuance
> ```

</details>

------------------------------------------------------------------------------------------

### 내 쿠폰 조회

<details>
 <summary><code>GET</code> <code><b>/api/v1/coupons/me</b></code> <code>(보유한 쿠폰 목록을 조회합니다)</code></summary>

##### Headers

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | X-Member-Id |  required | long   | 회원 ID  |

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `{ "count": 2, "coupons": [ { "id": 1, "name": "SPRING_SALE", "discountPercentage": 10, "expiredAt": "2025-03-21T13:00:00", "usingAt": "2025-03-20T13:00:00" } ] }` |

##### Example cURL

> ```bash
>  curl -X GET -H "X-Member-Id: 1" http://localhost:8080/api/v1/coupons/me
> ```

</details>

------------------------------------------------------------------------------------------

## 주문 API

### 주문 생성

<details>
 <summary><code>POST</code> <code><b>/api/v1/orders</b></code> <code>(주문을 생성합니다)</code></summary>

##### Headers

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | X-Member-Id |  required | long   | 회원 ID  |

##### Request Body

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | couponSummaryId | optional | long   | 쿠폰 요약 ID  |
> | orderItems | required | array  | 주문 상품 목록  |
> | paymentSummary | required | object | 결제 정보  |

> orderItems 예시:
> ```json
> [
>   { "productSummaryId": 1, "price": 10000, "quantity": 2 }
> ]
> ```
> paymentSummary 예시:
> ```json
> { "method": "POINT", "totalAmount": 20000, "discountAmount": 2000, "chargeAmount": 18000 }
> ```

##### Request Body Example

> ```json
> {
>   "couponSummaryId": 1,
>   "orderItems": [
>     { "productSummaryId": 1, "price": 1790000, "quantity": 1 },
>     { "productSummaryId": 2, "price": 89000, "quantity": 1 }
>   ],
>   "paymentSummary": {
>     "method": "POINT",
>     "totalAmount": 1879000,
>     "discountAmount": 187900,
>     "chargeAmount": 1691100
>   }
> }
> ```

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `{ "orderId": 1, "paymentSummary": { ... }, "orderItems": [ ... ] }` |
> | `404`         | `application/json`                | `{ "message": "보유하지 않은 쿠폰입니다." }` |
> | `400`         | `application/json`                | `{ "message": "지원하지 않는 결제수단입니다." }` |
> | `409`         | `application/json`                | `{ "message": "잔고의 잔액이 부족합니다." }`<br>`{ "message": "'상품명'의 재고가 부족합니다." }` |

##### Example cURL

> ```bash
>  curl -X POST -H "X-Member-Id: 1" -H "Content-Type: application/json" \
>       --data '{ ... }' \
>       http://localhost:8080/api/v1/orders
> ```

</details>

------------------------------------------------------------------------------------------

## 에러 응답 패턴

- 400 Bad Request: 필수 헤더/파라미터 누락, 잘못된 입력, 잘못된 금액, 잘못된 조회 파라미터, 지원하지 않는 결제수단 등
- 404 Not Found: 존재하지 않는 리소스(잔고, 보유하지 않은 쿠폰 등)
- 409 Conflict: 쿠폰 발급 정책 위반, 수량 초과, 기간 초과, 잔고 부족, 상품 재고 부족 등
- 응답 예시: `{ "message": "상세 메시지" }`
