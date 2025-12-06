# 콘서트 예약 서비스 API 명세서

## 개요
콘서트 예약 서비스의 REST API 명세서입니다. 대기열 시스템을 통해 안정적인 예약 서비스를 제공하며, 모든 API는 대기열 토큰 검증을 통과해야 사용 가능합니다.

## 1. 콘서트 조회 기능

### 1.1 예약 가능한 날짜 조회

```
GET /api/v1/concerts/available-dates
```

**설명**: 예약 가능한 콘서트 날짜 목록을 조회합니다.

**Request Headers**:
- `Authorization`: Bearer {token} (대기열 토큰)

**Response**:
```json
{
  "availableDates": [
    {
      "concertId": 1,
      "date": "2024-12-25",
      "title": "크리스마스 콘서트",
      "venue": "올림픽홀",
      "totalSeats": 50,
      "availableSeats": 35
    },
    {
      "concertId": 2,
      "date": "2024-12-31",
      "title": "신년 카운트다운 콘서트",
      "venue": "잠실 체조경기장",
      "totalSeats": 50,
      "availableSeats": 42
    }
  ]
}
```

### 1.2 특정 날짜의 좌석 조회

```
GET /api/v1/concerts/{concertId}/seats
```

**설명**: 특정 콘서트의 예약 가능한 좌석 정보를 조회합니다.

**Request Headers**:
- `Authorization`: Bearer {token} (대기열 토큰)

**Path Parameters**:
- `concertId` (required): 콘서트 ID

**Response**:
```json
{
  "concertId": 1,
  "date": "2024-12-25",
  "title": "크리스마스 콘서트",
  "seats": [
    {
      "seatNumber": 1,
      "status": "AVAILABLE",
      "price": 100000
    },
    {
      "seatNumber": 2,
      "status": "RESERVED",
      "price": 100000,
      "reservedUntil": "2024-12-20T15:05:00"
    },
    {
      "seatNumber": 3,
      "status": "SOLD",
      "price": 100000
    }
  ]
}
```

**Seat Status**:
- `AVAILABLE`: 예약 가능
- `RESERVED`: 임시 예약 (5분간 유지)
- `SOLD`: 결제 완료

## 2. 예약/결제 기능 (클린 아키텍처)

### 2.1 좌석 예약 요청

```
POST /api/v1/reservations
```

**설명**: 특정 좌석을 임시 예약합니다. (5분간 유지)

**Request Headers**:
- `Authorization`: Bearer {token} (대기열 토큰)

**Request Body**:
```json
{
  "concertId": 1,
  "seatNumber": 15,
  "userId": 123
}
```

**Response**:
```json
{
  "reservationId": "res_12345",
  "concertId": 1,
  "seatNumber": 15,
  "userId": 123,
  "status": "RESERVED",
  "price": 100000,
  "reservedAt": "2024-12-20T15:00:00",
  "expiresAt": "2024-12-20T15:05:00"
}
```

**Error Responses**:
- `400 Bad Request`: 이미 예약된 좌석
- `404 Not Found`: 존재하지 않는 콘서트 또는 좌석
- `401 Unauthorized`: 유효하지 않은 대기열 토큰

### 2.2 결제 처리

```
POST /api/v1/payments
```

**설명**: 예약된 좌석에 대해 결제를 진행합니다.

**Request Headers**:
- `Authorization`: Bearer {token} (대기열 토큰)

**Request Body**:
```json
{
  "reservationId": "res_12345",
  "userId": 123,
  "paymentMethod": "POINT"
}
```

**Response**:
```json
{
  "paymentId": "pay_67890",
  "reservationId": "res_12345",
  "userId": 123,
  "amount": 100000,
  "paymentMethod": "POINT",
  "status": "COMPLETED",
  "paidAt": "2024-12-20T15:03:30",
  "reservation": {
    "concertId": 1,
    "seatNumber": 15,
    "status": "SOLD"
  }
}
```

**Error Responses**:
- `400 Bad Request`: 예약이 만료되었거나 잔액 부족
- `404 Not Found`: 존재하지 않는 예약
- `409 Conflict`: 이미 결제된 예약

## 3. 포인트 충전 기능

### 3.1 포인트 충전

```
POST /api/v1/points/charge
```

**설명**: 사용자의 포인트를 충전합니다.

**Request Headers**:
- `Authorization`: Bearer {token} (대기열 토큰)

**Request Body**:
```json
{
  "userId": 123,
  "amount": 50000,
  "description": "포인트 충전"
}
```

**Response**:
```json
{
  "id": 1,
  "userId": 123,
  "balance": 150000,
  "createdAt": "2024-12-20T14:30:00",
  "updatedAt": "2024-12-20T15:00:00"
}
```

**Validation**:
- `amount`: 1원 이상 (필수)
- `userId`: 양수 (필수)

### 3.2 포인트 잔액 조회

```
GET /api/v1/points/balance/{userId}
```

**설명**: 사용자의 현재 포인트 잔액을 조회합니다.

**Request Headers**:
- `Authorization`: Bearer {token} (대기열 토큰)

**Path Parameters**:
- `userId` (required): 사용자 ID

**Response**:
```json
{
  "id": 1,
  "userId": 123,
  "balance": 150000,
  "createdAt": "2024-12-20T14:30:00",
  "updatedAt": "2024-12-20T15:00:00"
}
```

### 3.3 포인트 사용 가능 여부 확인

```
GET /api/v1/points/can-use/{userId}?amount={amount}
```

**설명**: 특정 금액의 포인트 사용 가능 여부를 확인합니다.

**Request Headers**:
- `Authorization`: Bearer {token} (대기열 토큰)

**Path Parameters**:
- `userId` (required): 사용자 ID

**Query Parameters**:
- `amount` (required): 확인할 금액

**Response**:
```json
{
  "canUse": true
}
```

### 3.4 포인트 사용 이력 조회

```
GET /api/v1/points/history/{userId}
```

**설명**: 사용자의 포인트 충전/사용 이력을 조회합니다.

**Request Headers**:
- `Authorization`: Bearer {token} (대기열 토큰)

**Path Parameters**:
- `userId` (required): 사용자 ID

**Response**:
```json
{
  "histories": [
    {
      "id": 1,
      "userId": 123,
      "amount": 50000,
      "type": "CHARGE",
      "description": "포인트 충전",
      "createdAt": "2024-12-20T15:00:00"
    },
    {
      "id": 2,
      "userId": 123,
      "amount": 100000,
      "type": "USE",
      "description": "콘서트 예약 결제",
      "createdAt": "2024-12-20T15:03:30"
    }
  ]
}
```

**Transaction Types**:
- `CHARGE`: 포인트 충전
- `USE`: 포인트 사용

## 공통 응답 형식

### 성공 응답
모든 API는 HTTP 상태 코드와 함께 JSON 형식으로 응답합니다.

### 에러 응답
```json
{
  "error": {
    "code": "INSUFFICIENT_BALANCE",
    "message": "잔액이 부족합니다.",
    "timestamp": "2024-12-20T15:00:00"
  }
}
```

### 공통 에러 코드
- `400 Bad Request`: 잘못된 요청 데이터
- `401 Unauthorized`: 인증 실패 (유효하지 않은 토큰)
- `403 Forbidden`: 권한 없음 (대기열 순서가 아님)
- `404 Not Found`: 리소스를 찾을 수 없음
- `409 Conflict`: 리소스 충돌 (이미 예약된 좌석 등)
- `500 Internal Server Error`: 서버 내부 오류

## 인증 및 대기열
모든 API는 대기열 토큰을 통한 인증이 필요하며, 대기열 순서가 되지 않은 사용자는 `403 Forbidden` 응답을 받습니다.

## 동시성 제어
- 좌석 예약: 비관적 락을 통한 동시성 제어
- 포인트 충전/사용: 비관적 락을 통한 잔액 정합성 보장
