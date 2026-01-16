# STEP 8: Application Event (아쉬운 사례)

## 개요
이 단계에서는 "실시간 예약정보(콘서트)"를 **데이터 플랫폼에 전송**하는 요구사항을 구현한다.

다만, 이 구현은 **"아쉬운 사례(문제가 있는 사례)"**를 의도적으로 보여주는 것을 목표로 한다.
다음 단계(STEP 9 이후)에서 이벤트 기반 비동기 처리로 이를 개선하는 과정을 보여줄 예정이다.

---

## 구현 내용

### 1. 데이터 플랫폼 Mock 클라이언트 (`DataPlatformClient`)
- **위치**: `src/main/java/kr/hhplus/be/server/infrastructure/external/DataPlatformClient.java`
- **역할**: 데이터 플랫폼 API를 호출하여 예약 정보를 전송
- **페이로드 구조**:
  ```
  - reservationId: 예약 ID
  - userId: 사용자 ID
  - concertId: 콘서트 ID
  - seatId: 좌석 ID
  - paidAmount: 결제 금액
  - occurredAt: 발생 시각
  ```

### 2. 예약 확정 로직 수정 (`ConfirmReservationInteractor.confirm()`)
예약 결제 확정 흐름에 데이터 플랫폼 전송 로직을 **직접 추가**했다:

```java
// 4. 데이터 플랫폼 전송 (트랜잭션 내부에서 동기 호출)
try {
    DataPlatformClient.ReservationEventPayload payload = 
            new DataPlatformClient.ReservationEventPayload(...);
    dataPlatformClient.postReservationEvent(payload);
} catch (Exception e) {
    throw e;  // 예외 발생 시 전체 롤백
}
```

---

## 현재 구현의 특징 (아쉬운 점)

### 1. 트랜잭션 내부 외부 API 호출
```
@Transactional
public void confirm(Command command) {
    // 1. DB 작업 (포인트 차감, 예약 상태 변경, 결제 기록)
    // ...
    
    // 2. 외부 API 호출 ← 문제!
    dataPlatformClient.postReservationEvent(payload);
    
    // 트랜잭션 커밋
}
```

**문제점**:
- 외부 API 호출이 데이터베이스 트랜잭션과 함께 처리됨
- 네트워크 지연(500ms mock delay)이 전체 API 응답시간에 영향
- 외부 호출 실패 시 DB 트랜잭션까지 롤백될 수 있음

### 2. 관심사 혼재
- 도메인 로직 (예약 확정, 포인트 차감, 좌석 상태 변경)과
- 외부 시스템 연동 로직 (데이터 플랫폼 전송, 페이로드 구성, HTTP 호출)이
한 서비스 클래스에 섞여 있음

### 3. 실패 처리의 모호함
```java
catch (Exception e) {
    throw e;  // 그냥 다시 던짐
}
```

**문제**:
- 데이터 플랫폼 전송 실패가 **예약 확정 전체를 실패**시킴
- 예약 확정(핵심 비즈니스)과 데이터 플랫폼 전송(보조 시스템)의 중요도가 구분되지 않음
- 일시적인 네트워크 장애(30초 recovery) vs 영구적인 데이터 손실을 구분하지 못함

---

## 테스트

### 1. 정상 케이스: 데이터 플랫폼 호출 확인
**클래스**: `DataPlatformIntegrationTest.confirm_reservation_calls_data_platform_test()`

```
Given:  예약 가능한 상태 (유저, 토큰, 좌석 준비)
When:   예약 충전 및 결제 확정
Then:   
  - 좌석 상태 = SOLD
  - 예약 상태 = CONFIRMED
  - 포인트 차감됨
  - 데이터 플랫폼 호출 1번 확인 ✓
```

### 2. 실패 케이스: 데이터 플랫폼 호출 실패 시 롤백
**클래스**: `DataPlatformIntegrationTest.data_platform_failure_causes_rollback_test()`

```
Given:  데이터 플랫폼 호출을 실패하도록 Mock 설정
When:   예약 확정 절차 진행
Then:
  - RuntimeException 발생 ✓
  - 예약 상태 = PENDING (롤백됨) ✓
  - 좌석 상태 = RESERVED (롤백됨) ✓
  - 포인트 그대로 10000L (업데이트 안 됨) ✓
```

이 테스트는 **문제점을 적극적으로 드러낸다**:
- 외부 시스템 장애가 핵심 비즈니스(예약 확정) 실패로 이어짐
- 고객 입장에서는 결제가 완료되지 않은 것처럼 보임

---

## 흐름도

```
[클라이언트]
    ↓
[ReservationController.confirmReservation()]
    ↓
[ConfirmReservationInteractor.confirm()] @Transactional
    ├─ 1. 예약 조회
    ├─ 2. 포인트 차감 (DB 업데이트)
    ├─ 3. 예약 상태: PENDING → CONFIRMED (DB 업데이트)
    ├─ 4. 좌석 상태: RESERVED → SOLD (DB 업데이트)
    ├─ 5. 결제 레코드 생성 (DB 저장)
    ├─ 6. 랭킹 정보 업데이트 (Redis)
    │
    ├─ [문제점] 7. 데이터 플랫폼 전송 (외부 HTTP 호출)
    │   ├─ 성공 → 트랜잭션 커밋 ✓
    │   └─ 실패 → 전체 트랜잭션 롤백 ✗
    │
    ↓
[DB 커밋 또는 롤백]
```

---

## 문서화 관점 요약

현재 구현은 **기능적으로 동작**한다:
- 예약 확정 성공 시 데이터 플랫폼에 정보 전송 ✓
- 통합 테스트로 호출 검증 ✓

그러나 **구조적 문제**가 명확하다:
1. **성능**: 외부 API 지연이 핵심 API 응답시간에 직접 영향
2. **신뢰성**: 외부 시스템 장애가 핵심 기능까지 영향
3. **유지보수성**: 관심사가 분리되지 않아 코드 복잡도 증가

다음 단계에서는 **Application Event와 비동기 처리**를 통해 이러한 문제들을 해결할 것이다.

---

## 참고: 금지 사항
이 단계에서는 다음을 의도적으로 사용하지 않았다:
- Kafka, RabbitMQ 등 메시지 브로커 (STEP 9 이후)
- Outbox 패턴, CDC (Change Data Capture) (고급서)
- Saga 패턴 (개념 언급 정도만)

이들은 다음 단계의 **개선 사항**으로 단계적으로 도입될 것이다.
