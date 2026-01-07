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
> | `200`         | `application/json`                | `{"balance": 50000}`                        |
> | `400`         | `application/json`                | `{"code":"BB001","message":"회원ID가 필요합니다"}` |
> | `401`         | `application/json`                | `{"code":"401","message":"인증실패"}`           |

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
> | `200`         | `application/json`                | `{"balance": 60000}`                        |
> | `400`         | `application/json`                | `{"code":"BB001","message":"회원ID가 필요합니다"}`  |
> | `401`         | `application/json`                | `{"code":"401","message":"인증실패"}`           |
> | `404`         | `application/json`                | `{"code":"404","message":"잔액정보를 확인할수없습니다"}` |

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

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `[{"productList": [{"productId": 1, "productName": "상품명", "price": 10000, "stockQuantity": 100}]}]` |
> | `400`         | `application/json`                | `{"code":"400","message":"잘못된 페이지 파라미터입니다"}`                   |

##### Example cURL

> ```bash
>  curl -X GET "http://localhost:8080/api/v1/products?page=0&size=10"
> ```

</details>

------------------------------------------------------------------------------------------

### 인기 상품 조회

<details>
 <summary><code>GET</code> <code><b>/api/v1/products/top-selling</b></code> <code>(최근 3일간 가장 많이 팔린 상위 5개 상품을 조회합니다)</code></summary>

##### Parameters

> None

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `[{"productList": [{"productId": 1, "productName": "인기상품", "price": 20000, "stockQuantity": 50}]}]` |
> | `500`         | `application/json`                | `{"code":"500","message":"인기 상품 조회 중 오류가 발생했습니다"}`            |

##### Example cURL

> ```bash
>  curl -X GET http://localhost:8080/api/v1/products/top-selling
> ```

</details>

------------------------------------------------------------------------------------------

## 쿠폰 API

### 선착순 쿠폰 발급

<details>
 <summary><code>POST</code> <code><b>/api/v1/coupons/first-come-first-serve</b></code> <code>(선착순 쿠폰을 발급받습니다)</code></summary>

##### Headers

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | X-Member-Id |  required | long   | 회원 ID  |

##### Parameters

> None

##### Request Body

> None

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `{"id": 1, "name": "신규 회원 할인 쿠폰", "discountPercentage": 10, "expiredAt": "2024-12-31T23:59:59"}` |
> | `400`         | `application/json`                | `{"code":"400","message":"회원 ID가 필요합니다"}`                        |
> | `409`         | `application/json`                | `{"code":"409","message":"이미 쿠폰을 발급받으셨습니다"}`                   |
> | `410`         | `application/json`                | `{"code":"410","message":"쿠폰이 모두 소진되었습니다"}`                     |

##### Example cURL

> ```bash
>  curl -X POST -H "X-Member-Id: 1" http://localhost:8080/api/v1/coupons/first-come-first-serve
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

##### Parameters

> None

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `{"coupons": [{"id": 1, "name": "10% 할인 쿠폰", "discountPercentage": 10, "expiredAt": "2024-12-31T23:59:59", "usingAt": "2024-01-15T10:30:00"}]}` |
> | `400`         | `application/json`                | `{"code":"400","message":"회원 ID가 필요합니다"}`                        |
> | `401`         | `application/json`                | `{"code":"401","message":"인증 실패"}`                               |

##### Example cURL

> ```bash
>  curl -X GET -H "X-Member-Id: 1" http://localhost:8080/api/v1/coupons/me
> ```

</details>

------------------------------------------------------------------------------------------

## 주문 API

### 주문하기

<details>
 <summary><code>POST</code> <code><b>/api/v1/orders</b></code> <code>(상품을 주문합니다)</code></summary>

##### Headers

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | X-Member-Id |  required | long   | 회원 ID  |

##### Parameters

> None

##### Request Body

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | productIdAndQuantityMap |  required | Map<Long, Long>   | 상품 ID와 수량 맵  |

##### Request Body Example

> ```json
> {
>   "productIdAndQuantityMap": {
>     "1": 2,
>     "2": 1,
>     "3": 5
>   }
> }
> ```

##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `{"orderId": 12345}`                                               |
> | `400`         | `application/json`                | `{"code":"400","message":"회원 ID가 필요합니다"}`                        |
> | `402`         | `application/json`                | `{"code":"402","message":"잔액이 부족합니다"}`                          |
> | `404`         | `application/json`                | `{"code":"404","message":"상품을 찾을 수 없습니다"}`                      |
> | `409`         | `application/json`                | `{"code":"409","message":"재고가 부족합니다"}`                          |

##### Example cURL

> ```bash
>  curl -X POST -H "Content-Type: application/json" -H "X-Member-Id: 1" \
>       --data '{"productIdAndQuantityMap": {"1": 2, "2": 1}}' \
>       http://localhost:8080/api/v1/orders
> ```

</details>

------------------------------------------------------------------------------------------
