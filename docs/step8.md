# STEP 8: Application Event (좋은 사례 - 개선된 버전)

## 개요
예약 확정 시 "실시간 예약정보(콘서트)"를 **데이터 플랫폼에 전송**하는 요구사항을 구현한다.

이전 단계(아쉬운 사례)에서 보았던 문제점들을:
- **Spring Application Event** + **@TransactionalEventListener**를 활용하여
- **트랜잭션과 외부 API 호출을 완벽하게 분리**하고
- **관심사를 명확하게 분리**하는 "좋은 사례"로 개선했다.

---

## 아쉬운 사례 vs 좋은 사례 비교

| 관점 | 아쉬운 사례 (Before) | 좋은 사례 (After) |
|------|-----------|----------|
| **구조** | 도메인 로직 내부에서 외부 API 직접 호출 | 이벤트 발행으로만 처리, 리스너에서 호출 |
| **실행 시점** | 트랜잭션 commit 직전 | 트랜잭션 commit **이후** (@AFTER_COMMIT) |
| **롤백 영향** | 외부 호출 실패 → 전체 롤백 | 롤백 시 외부 호출 **발생하지 않음** |
| **외부 실패 영향** | 도메인 로직까지 실패 | 도메인은 완료, 플랫폼만 실패 |
| **관심사 분리** | 혼재 (비즈니스 + 연동) | 분리 (비즈니스 ⊥ 연동) |
| **응답시간** | 외부 지연 포함 | 외부 지연 제외 |
| **추적성** | 미흡 | eventId로 추적 가능 |

---

## 왜 이벤트가 필요한가?

### 1. 트랜잭션 경계의 명확화
```
[아쉬운 사례]
@Transactional
public void confirm() {
    // DB 작업 1, 2, 3...
    dataPlatformClient.call();  // 트랜잭션 내 외부 호출 ← 문제!
    // 외부 호출 실패 → 전체 롤백
}

[좋은 사례]
@Transactional
public void confirm() {
    // DB 작업 1, 2, 3...
    applicationEventPublisher.publishEvent(event);  // 발행만 수행
    // 트랜잭션 내에서는 발행만 → 빠름
    // 실제 외부 호출은 commit 이후 → 안전
}
```

### 2. 관심사의 명확한 분리
- **ConfirmReservationInteractor**: 예약 확정 (포인트, 좌석 상태, 결제 기록)
- **ReservationCompletedEventListener**: 데이터 플랫폼 전송 (외부 연동)

### 3. 외부 시스템 장애 격리
- 데이터 플랫폼이 장애 중도 예약은 정상 완료
- 도메인 로직을 외부 의존성으로부터 보호

---

## 개선된 구현 내용

### 1. 이벤트 정의 (`ReservationCompletedEvent`)
```java
public class ReservationCompletedEvent extends ApplicationEvent {
    private final String eventId;        // UUID로 추적
    private final Long reservationId;
    private final Long userId;
    private final Long concertId;
    private final Long seatId;
    private final Long paidAmount;
    private final LocalDateTime occurredAt;
}
```

**특징**:
- `eventId`: UUID로 고유 식별 (로그 추적 용)
- `ApplicationEvent` 상속: Spring 이벤트 인프라 활용
- 필요한 정보만 포함 (Payload와 중복 제외)

### 2. 이벤트 발행 (`ConfirmReservationInteractor`)
```java
@Transactional
public void confirm(Command command) {
    // 1. 포인트 차감
    member.usePoints(seat.getPrice());
    memberRepository.save(member);
    
    // 2. 예약 & 좌석 상태 변경
    reservation.confirm();
    seatRepository.save(reservation);
    seatRepository.save(seat);
    
    // 3. 결제 기록
    paymentRepository.save(payment);
    
    // [개선점] 4. 이벤트 발행 (외부 호출은 여기서 하지 않음)
    ReservationCompletedEvent event = new ReservationCompletedEvent(
            this, 
            reservation.getId(), 
            member.getId(),
            seat.getConcertId(),
            seat.getId(),
            seat.getPrice(),
            LocalDateTime.now()
    );
    applicationEventPublisher.publishEvent(event);  // 발행만 수행
    
    // 5. 랭킹 정보 업데이트
    concertRankingService.updateSalesInfo(...);
}
```

**개선점**:
- `DataPlatformClient` 의존성 제거
- 외부 호출 로직 완전 제거 (이벤트만 발행)
- 따라서 기한 단축, 실패 시 롤백 가능성 없음

### 3. 이벤트 리스너 (`ReservationCompletedEventListener`)
```java
@Component
public class ReservationCompletedEventListener {
    
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleReservationCompleted(ReservationCompletedEvent event) {
        try {
            log.info("예약 확정 이벤트 처리: eventId={}", event.getEventId());
            
            // 데이터 플랫폼 전송 (트랜잭션 commit 이후)
            DataPlatformClient.ReservationEventPayload payload = 
                    new DataPlatformClient.ReservationEventPayload(...);
            dataPlatformClient.postReservationEvent(payload);
            
            log.info("예약 확정 이벤트 처리 완료: eventId={}", event.getEventId());
        } catch (Exception e) {
            // 외부 호출 실패 시:
            // - 도메인 트랜잭션은 이미 커밋됨 (영향 없음)
            // - 실패를 로그로 남김 (eventId로 추적 가능)
            log.error("예약 확정 이벤트 처리 실패: eventId={}, error={}",
                    event.getEventId(), e.getMessage(), e);
        }
    }
}
```

**핵심**:
- `@TransactionalEventListener(phase = AFTER_COMMIT)`:
  - 이 annotation이 모든 것을 해결함!
  - 트랜잭션이 **성공적으로 커밋된 후**에만 실행
  - 롤백되면 리스너가 호출되지 않음
  
- `try-catch` 블록:
  - 외부 호출 실패를 graceful하게 처리
  - 도메인 로직에 영향을 주지 않음
  - eventId로 어떤 예약이 실패했는지 추적 가능

---

## AFTER_COMMIT 선택 이유

### 왜 AFTER_COMMIT을 사용하는가?

```
[rollback 시나리오]

포인트 부족으로 member.usePoints() 실패
    ↓
@Transactional 롤백
    ↓
@TransactionalEventListener(AFTER_COMMIT)이 실행되지 않음
    ↓
DataPlatformClient 호출 안 됨 ✓
    ↓
"롤백되었는데 플랫폼으로는 전송된" 부작용이 발생하지 않음
```

다른 선택지들:
- `BEFORE_COMMIT`: 리스너 실패 시 롤백 가능 → 외부 호출 실패가 도메인에 영향
- `AFTER_ROLLBACK`: 이벤트 발행되지 않음
- 기본값 (동기 리스너): 트랜잭션 내에서 실행 → 외부 호출 지연 포함

**결론**: `AFTER_COMMIT`이 유일한 정답

---

## 실패 대응 방침

### 현재 수준 (STEP 8)
- 실패 로깅 (eventId 포함)
- 도메인 트랜잭션 보호 (롤백 방지)
- 수동 개입 가능 (로그 검색 → 재전송)

### 왜 현재 단계에서는 재시도를 안 할까?
- 재시도 로직을 추가하려면 이벤트 저장소/상태 추적 필요
- 복잡도 증가 → 개념 학습에 방해
- 먼저 "기본 구조"를 이해한 후 개선하는 것이 교육적

---

## 테스트 케이스

### A) 커밋 이후에만 플랫폼 호출 발생

**테스트**: `ReservationEventIntegrationTest.confirm_reservation_calls_platform_after_commit()`

```
Given:  유저(포인트 충분), 토큰, 좌석 준비
When:   예약 확정 (confirm())
Then:   
  - 좌석 상태 = SOLD ✓
  - 예약 상태 = CONFIRMED ✓
  - 포인트 차감 ✓
  - DataPlatformClient.postReservationEvent() 호출 1회 ✓
```

**중요점**: 
- 트랜잭션이 성공적으로 커밋됨
- 그 **이후에** 플랫폼 호출이 발생
- 동기 리스너가 아니므로 confirm() 반환 시점은 이미 지남

---

### B) 롤백 시 플랫폼 호출 발생하지 않음

**테스트**: `ReservationEventIntegrationTest.rollback_due_to_insufficient_points_prevents_platform_call()`

```
Given:  포인트 부족 (필요 5000원, 보유 3000원)
When:   좌석 예약 후 결제 확정 시도
Then:
  - IllegalArgumentException 발생 ✓
  - 트랜잭션 롤백 ✓
  - 예약 상태 = PENDING (변경 안 됨) ✓
  - DataPlatformClient 호출 0회 ✓
```

**핵심 검증**:
```java
// AFTER_COMMIT 덕분에 리스너가 실행되지 않음
verify(dataPlatformClient, times(0)).postReservationEvent(any());
```

롤백 시 이벤트 리스너가 **호출되지 않는 것** 자체가 본 개선의 증거

---

### C) 플랫폼 호출 실패가 도메인 트랜잭션에 영향 없음

**테스트**: `ReservationEventIntegrationTest.platform_failure_does_not_affect_domain_transaction()`

```
Given:  DataPlatformClient.postReservationEvent()를 실패하도록 mock 설정
When:   예약 확정 수행
Then:
  - confirm() 메서드 예외 발생 안 함 ✓
  - 좌석 상태 = SOLD (커밋됨) ✓
  - 예약 상태 = CONFIRMED (커밋됨) ✓
  - 포인트 = 차감됨 (커밋됨) ✓
  - DataPlatformClient 호출 1회 시도했으나 실패 (로그 기록) ✓
```

**아쉬운 사례와의 차이**:
```
[아쉬운 사례]
외부 호출 실패 → RuntimeException 발생 → 전체 롤백

[좋은 사례]
외부 호출은 AFTER_COMMIT 이후 → 도메인은 이미 커밋됨
→ 호출 실패 시 로그만 남김, 도메인 로직에 영향 없음
```

---

## 흐름도

### 성공 케이스
```
[클라이언트]
    ↓
[ReservationController.confirmReservation()]
    ↓
[ConfirmReservationInteractor.confirm()] @Transactional
    ├─ 1. 포인트 차감 (DB)
    ├─ 2. 예약 상태 변경 (DB)
    ├─ 3. 결제 기록 생성 (DB)
    ├─ 4. 이벤트 발행 (메모리)
    └─ 5. 랭킹 정보 업데이트 (Redis)
    
    ║ 트랜잭션 commit
    ↓
    
[ReservationCompletedEventListener] @TransactionalEventListener(AFTER_COMMIT)
    ├─ eventId 확인: abc-123
    └─ DataPlatformClient.postReservationEvent() 호출
    
    ║ 플랫폼 전송 완료
    ↓
    
[클라이언트] 응답 (201 Created)
```

### 롤백 케이스
```
[ConfirmReservationInteractor.confirm()] @Transactional
    ├─ 포인트 차감 시도
    └─ 부족한 포인트로 예외 발생
    
    ║ 트랜잭션 rollback
    ↓
    
[ReservationCompletedEventListener] 실행 안 됨
    └─ AFTER_COMMIT이므로 리스너가 호출되지 않음
    
    ║ DataPlatformClient 호출 0회
    ↓
    
[클라이언트] 응답 (400 Bad Request)
```

---

## 구현 파일 정리

| 파일 | 역할 | 상태 |
|------|------|------|
| `ReservationCompletedEvent.java` | 이벤트 정의 | 신규 |
| `ConfirmReservationInteractor.java` | 이벤트 발행 | 수정 (외부 호출 제거) |
| `ReservationCompletedEventListener.java` | 이벤트 처리 | 신규 |
| `DataPlatformClient.java` | 플랫폼 호출 | 기존 (변경 없음) |
| `ReservationEventIntegrationTest.java` | 개선된 테스트 | 신규 (3개 케이스) |

---

## 문서화 관점 요약

### 개선 전 (아쉬운 사례)
```
문제점:
✗ 트랜잭션 내부에서 외부 API 동기 호출
✗ 도메인 로직과 연동 로직 혼재
✗ 외부 실패가 도메인 롤백 유발
✗ 성능 이슈 (외부 지연 포함)
✗ 추적 불가 (실패 원인 규명 어려움)
```

### 개선 후 (좋은 사례)
```
개선사항:
✓ 이벤트로 트랜잭션과 외부 호출 분리
✓ 도메인 로직 ⊥ 연동 로직 (관심사 분리)
✓ AFTER_COMMIT으로 안전성 보장
✓ 성능 개선 (외부 지연 제외)
✓ eventId로 추적 가능 (운영성 향상)
✓ 외부 실패 → 로그 기록, 도메인 영향 없음
```

---

## FAQ

**Q: 왜 ReservationCompletedEvent가 ApplicationEvent를 상속해야 하나?**
A: Spring의 이벤트 발행/구독 인프라를 활용하기 위함. ApplicationEventPublisher와 @TransactionalEventListener가 연동됨.

**Q: 만약 리스너에서 다중 외부 호출이 필요하면?**
A: 이 리스너 내에서 여러 클라이언트를 의존성 주입받아 사용하면 됨. 단, 각 호출은 독립적으로 fail-safe 처리.

**Q: Kafka 같은 메시지 브로커 대신 왜 이벤트만 사용?**
A: STEP 8은 기본 개념 학습이 목표. Kafka는 프로세스 외 이벤트 저장이 필요 → STEP 9에서 도입.

---

## 결론

STEP 8에서는 **Application Event + @TransactionalEventListener**를 활용하여:
1. **트랜잭션과 외부 호출 분리**
2. **관심사 명확화**
3. **도메인 로직 보호**
4. **추적 가능한 실패 처리**

이를 통해 마이크로서비스 시대의 기본 아키텍처 패턴을 습득하게 된다.
STEP 9에서는 이를 바탕으로 "보장된 전송"까지 진화시킬 예정이다.
