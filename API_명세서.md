# API 명세서

## CommonResponse 형식

```json
{
  "code": 0,
  "message": "",
  "result": {}
}
```

성공응답일 경우 code는 0, message는 빈 문자열.  
에러응답일 경우 -1, message에 에러 메시지 리턴.

result에 실제 결과데이터가 담긴다.

## 유저 대기열 토큰 조회

**GET** `/token/{userId}`
사용자 Id로 생성된 대기열 토큰을 조회한다.  
만약 대기열에 처음 들어왔다면 신규 생성하여 리턴한다.

**Parameters**

| 파라미터   | 타입     | 필수 | 설명           | 기본값 |  
|--------|--------|----|--------------|-----|
| userId | string | Y  | 사용자 ID(uuid) |     |

**Response**

```json
{
  "userId": "USER_UUID",
  "tokenId": "TOKEN_UUID",
  "status": "WAIT | ACTIVE",
  "remainingSeconds": 6000
}
```

## 예약가능 콘서트 날짜 조회

**GET** `/concerts/{concertId}/schedules`  
특정 콘서트의 예약 가능한 날짜를 조회한다.

**Parameters**

| 파라미터      | 타입     | 필수 | 설명           | 기본값 |  
|-----------|--------|----|--------------|-----|
| concertId | string | Y  | 콘서트 ID(uuid) |     |

**Header**

```
QUEUE-TOKEN: {queue_token_uuid}
```

**Response**

```json
{
  "concertId": "CONCERT_UUID",
  "availableScheduleList": [
    {
      "concertScheduleId": "SCHEDULE_UUID",
      "concertDate": "2025-12-26 19:00:00",
      "availableSeatCount": 28
    },
    {
      "concertScheduleId": "SCHEDULE_UUID",
      "concertDate": "2025-12-27 19:00:00",
      "availableSeatCount": 3
    },
    {
      "concertScheduleId": "SCHEDULE_UUID",
      "concertDate": "2025-12-28 19:00:00",
      "availableSeatCount": 5
    }
  ]
}
```

## 예약가능 좌석 조회

**GET** `/concerts/{concertId}/schedules/{scheduleId}/seats`  
특정 콘서트의 특정 날짜의 예약가능한 좌석을 조회한다.

**Header**

```
QUEUE-TOKEN: {queue_token_uuid}
```

**Parameters**

| 파라미터       | 타입     | 필수 | 설명           | 기본값 |  
|------------|--------|----|--------------|-----|
| concertId  | string | Y  | 콘서트 ID(uuid) |     |
| scheduleId | string | Y  | 스케쥴 ID(uuid) |     |

```json
{
  "concertId": "CONCERT_UUID",
  "concertScheduleId": "SCHEDULE_UUID",
  "concertDate": "2025-12-28 19:00:00",
  "availableSeatCount": 5,
  "availableSeatList": [
    1,
    23,
    36,
    37,
    49
  ]
}
```

## 좌석 예약 요청

**POST** `/reservation`  
좌석 예약을 요청한다.  
우선 PENDING 상태의 예약을 리턴받는다.  
결제 완료시 CONFIRM상태로 전환된다.

**Header**

```
QUEUE-TOKEN: {queue_token_uuid}
```

**RequestBody**

```json
{
  "scheduleId": "SCHEDULE_UUID",
  "seats": [
    36,
    37
  ]
}

```

**Response**

```json
{
  "reservationId": "RESERVATION_UUID",
  "STATUS": "PENDING"
}
```

## 잔액 조회

**GET** `/users/{userId}/point`  
사용자의 결제 포인트를 조회한다.

**Parameters**

| 파라미터   | 타입     | 필수 | 설명           | 기본값 |  
|--------|--------|----|--------------|-----|
| userId | string | Y  | 사용자 ID(uuid) |     |

```
QUEUE-TOKEN: {queue_token_uuid}
```

**Response**

```json
{
  "userId": "USER_UUID",
  "point": 98000
}
```

## 잔액 충전

**POST** `/users/{userId}/point/charge`  
사용자의 결제포인트를 충전한다.  
PathVariable로 사용자ID를, 충전할 포인트를 RequestBody로 입력받는다.

**RequestBody**

```json
{
  "point": 10000
}
```

**Response**

```json
{
  "userId": "USER_UUID",
  "point": 98000
}
```

## 결제

**POST** `/reservation/{reservationId}`  
PENDING 상태의 예약을 결제하여 확정한다.

| 파라미터          | 타입     | 필수 | 설명          | 기본값 |  
|---------------|--------|----|-------------|-----|
| reservationId | string | Y  | 예약 ID(uuid) |     |

```
QUEUE-TOKEN: {queue_token_uuid}
```

**Response**

```json
{
  "reservationId": "RESERVATION_UUID",
  "STATUS": "CONFIRMED"
}
```
