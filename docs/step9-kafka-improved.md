# STEP 9: Kafka (개선 사례 - 좋은 사례)

## 개요

이 문서는 STEP9 Kafka의 **개선 사례(좋은 사례)**를 설명합니다.

기초 사례에서는 "Kafka로 메시지를 보낸다"는 기본 동작에 집중했다면,
개선 사례에서는 **정합성, 멱등성, 버전 관리**를 포함한 **프로덕션 수준의 설계**를 다룹니다.

---

## 핵심 개선 사항

### 1. 커밋 이후 발행 보장 (TransactionSynchronizationManager)

**문제**: 기초 사례에서는 트랜잭션 중간에 Kafka 메시지를 발행했습니다.
```java
// [기초 사례 - 문제]
@Transactional
public void confirm() {
    reservation.confirm();
    reservationRepository.save(reservation);
    
    kafkaTemplate.send(TOPIC, message);  // 트랜잭션 중간에 발행
    
    // 아래 코드에서 예외 발생 시?
    // → Kafka 메시지는 이미 발행됨 (정합성 깨짐)
    seat.confirm();
    seatRepository.save(seat);
}
```

**해결책**: TransactionSynchronizationManager로 커밋 이후 발행을 **명시적으로 코드화**

```java
// [개선 사례 - 해결]
@Transactional
public void confirm() {
    reservation.confirm();
    reservationRepository.save(reservation);
    seat.confirm();
    seatRepository.save(seat);
    
    // 트랜잭션이 성공적으로 커밋된 '이후에' 발행
    registerKafkaPublishAfterCommit(message);
    
    // 아래 예외들은 메시지 발행에 영향 없음 (정합성 보장)
}

private void registerKafkaPublishAfterCommit(ReservationEventMessage message) {
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(TOPIC, key, message);
            }
            
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    log.warn("트랜잭션 롤백 - 메시지 발행 취소");
                }
            }
        }
    );
}
```

**이점**:
- ✅ 롤백 시 메시지 발행 보지 않음
- ✅ 메시지 발행 실패가 도메인 트랜잭션에 영향 없음
- ✅ 코드로 "commit-after-send" 보장이 분명함

---

### 2. Topic/Key/Version 설계

#### Topic: 버전 포함

```
concert.reservation.completed.v1
                           ↑
                         버전
```

**이유**:
- 향후 스키마 변경 시 v2를 새로 만들 수 있음
- v1 Consumer는 계속 v1을 소비, v2 Consumer는 v2를 소비
- 무중단 마이그레이션 가능

#### Key: reservationId로 고정

```java
String key = message.getReservationId().toString();
kafkaTemplate.send(TOPIC, key, message);
```

**Partition 배치**:
```
Topic: concert.reservation.completed.v1
Partition 0: reservationId=1, 4, 7, ... (hash(key) % 3 == 0)
Partition 1: reservationId=2, 5, 8, ... (hash(key) % 3 == 1)
Partition 2: reservationId=3, 6, 9, ... (hash(key) % 3 == 2)
```

**이점**:
- 같은 예약(reservationId)의 메시지는 항상 **같은 partition**으로 들어감
- Partition 내에서는 **순서 보장** (같은 예약의 메시지들은 순서대로 처리)
- Consumer 간 병렬 처리 가능 (partition별로 다른 consumer가 처리)

---

### 3. Payload 설계 (eventId 포함)

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",  // ← 멱등성 키
  "reservationId": 12345,
  "userId": 1,
  "concertId": 5,
  "seatId": 42,
  "paidAmount": 150000,
  "occurredAt": "2026-01-28T01:50:00"
}
```

**eventId의 역할**:
- 메시지의 고유 식별자 (UUID)
- Consumer 멱등성의 기반
- 추적/감사 로깅의 기반

---

## Consumer 멱등성 (At-least-once 대응)

### 문제: Kafka의 at-least-once 보장

Kafka는 기본적으로 **at-least-once** 만을 보장합니다.

즉, 같은 메시지가 **여러 번** 전달될 수 있습니다:
- 네트워크 재시도
- Consumer 크래시 후 재시작 (rebalancing)
- Offset 커밋 실패

### 해결책: eventId 기반 중복 제거

**processed_events 테이블** 도입:

```sql
CREATE TABLE processed_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL UNIQUE,        -- ← eventId
    reservation_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,                  -- SUCCESS or FAILED
    processed_at DATETIME NOT NULL,
    failure_reason TEXT
);
```

**Consumer 로직**:

```java
@Transactional
public void handleReservationCompleted(ReservationEventMessage message) {
    String eventId = message.getEventId();
    
    // [1단계] 멱등성 검증
    if (processedEventRepository.existsByEventId(eventId)) {
        log.warn("이미 처리된 이벤트 - 스킵: eventId={}", eventId);
        return;  // ← 중복 처리 방지!
    }
    
    try {
        // [2단계] 처리
        dataPlatformClient.postReservationEvent(payload);
        
        // [3단계] 기록 (SUCCESS)
        processedEventRepository.save(
            ProcessedEventEntity.builder()
                .eventId(eventId)
                .reservationId(message.getReservationId())
                .status("SUCCESS")
                .processedAt(now())
                .build()
        );
    } catch (Exception e) {
        // [4단계] 삼패 기록
        processedEventRepository.save(
            ProcessedEventEntity.builder()
                .eventId(eventId)
                .reservationId(message.getReservationId())
                .status("FAILED")
                .failureReason(e.getMessage())
                .processedAt(now())
                .build()
        );
        throw e;
    }
}
```

### 멱등성 보장 시나리오

#### 시나리오 1: 정상 처리
```
[Kafka] 메시지 발행: eventId=111
    ↓
[Consumer] 수신 → 처리 시작 (existsByEventId = false)
    ↓
[DataPlatform] 호출 성공
    ↓
[DB] processed_events에 (eventId=111, status=SUCCESS) 저장
    ↓
[Kafka Offset] 자동 커밋
```

#### 시나리오 2: 반복 수신 (네트워크 재시도)
```
[Kafka] 메시지 재발행: eventId=111  (동일 메시지)
    ↓
[Consumer] 수신 → 처리 시작
    ↓
[DB] existsByEventId(111) = true  ← 이미 있음!
    ↓
[Consumer] 로그만 남기고 `return`
    ↓
[DataPlatform] 호출 안 됨! (멱등성 보장)
```

**이점**: 정확히 한 번 처리 효과 달성!

---

## Partition & Consumer Group의 병렬 처리

### 아키텍처

```
Topic: concert.reservation.completed.v1 (3개 partition)
│
├─ Partition 0: [msg1, msg4, msg7, ...]
├─ Partition 1: [msg2, msg5, msg8, ...]
└─ Partition 2: [msg3, msg6, msg9, ...]

Consumer Group: concert-reservation-consumer-group (3개 consumer)
│
├─ Consumer 1: Partition 0만 처리
├─ Consumer 2: Partition 1만 처리
└─ Consumer 3: Partition 2만 처리

각 partition 내에서는 순서 보장
서로 다른 partition은 병렬 처리
```

### Rebalancing 이해

Consumer 추가/제거 시 자동으로 partition 재할당:

```
초기 상태:
Consumer 1 → Partition [0, 1]
Consumer 2 → Partition [2]

↓ Consumer 3 추가

Rebalancing 시작 (잠시 중단):
Consumer 1 → Partition [0]
Consumer 2 → Partition [1]
Consumer 3 → Partition [2]

↓ Rebalancing 완료

병렬 처리 강화됨!
```

### Offset 개념

```
Partition 0:
msg1(offset=0)
msg4(offset=1)
msg7(offset=2)
↑
consumer.position = 2  ← 다음 메시지는 offset 3부터 읽음

Consumer Group이 offset 관리:
concert-reservation-consumer-group:
  Partition 0: offset=2
  Partition 1: offset=5
  Partition 2: offset=3
```

### Offset Commit 전략

**자동 (enable.auto.commit=true)**:
```
Consumer 처리 완료
    ↓
자동으로 offset 커밋
    ↓
다시 시작하면 그 다음부터 읽음
```

**수동**:
```
Consumer 처리 완료
    ↓
consumer.commitSync() or commitAsync()
    ↓
명시적 컨트롤로 더 안전
```

---

## 실패 처리 전략

### Dead Letter Queue (DLQ)

처리 실패한 메시지를 별도 토픽으로 분류:

```
기본 Topic: concert.reservation.completed.v1
     ↓
[Consumer 처리]
   ├─ 성공 → processed_events (SUCCESS)
   └─ 실패 → DLQ 토픽

DLQ Topic: concert.reservation.completed.dlq.v1
     ↓
[별도 모니터링/수동 검사/재처리]
```

### 로깅 기반 추적

```java
log.error("데이터 플랫폼 전송 실패: eventId={}, reservationId={}, error={}", 
    eventId, reservationId, e.getMessage(), e);
    
// 로그 검색: eventId=550e8400-e29b-41d4-a716-446655440000
// → 어느 예약의 실패인지, 언제 실패했는지 조회 가능
```

---

## 로컬 검증 시나리오

### A) 성공 케이스

```bash
# Terminal 1: Kafka 클러스터 시작
docker-compose -f docker-compose.kafka.yaml up -d

# Terminal 2: 애플리케이션 실행
./gradlew bootRun

# Terminal 3: 예약 확정 호출
curl -X POST http://localhost:8080/api/reservations/1/confirm

# 로그 확인:
# 1. Producer 로그:
#    "Kafka 메시지 발행 등록 (커밋 이후): eventId=550e8400..."
#    "Kafka 메시지 발행 완료 (커밋 이후): eventId=550e8400..., reservationId=1"
#
# 2. Consumer 로그:
#    "Kafka 메시지 수신: eventId=550e8400..., reservationId=1"
#    "데이터 플랫폼 전송 완료: eventId=550e8400..., reservationId=1"
#
# 3. DB 확인:
#    SELECT * FROM processed_events WHERE event_id='550e8400...';
#    → status='SUCCESS' 기록됨
```

### B) 실패 방지 (롤백) 케이스

```java
// ConfirmReservationInteractor 수정 (테스트 용도)
if (seat.getPrice() > 100000) {  // 고가 좌석
    throw new IllegalArgumentException("가격 너무 높음");  // 故의로 실패
}
```

**결과**:
```
[트랜잭션 롤백]
    ↓
[Producer 로그]
"트랜잭션 롤백 - Kafka 메시지 발행 취소: eventId=550e8400..."
    ↓
[DB processed_events]
레코드 없음! (발행되지 않음)
    ↓
[Kafka]
메시지가 Kafka에 발행되지 않음 (정합성 보장!)
```

### C) 멱등성 테스트 (같은 메시지 2번 수신)

```bash
# 메시지를 수동으로 Kafka에 재발행
docker exec broker1 kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic concert.reservation.completed.v1 \
  --property "parse.key=true" \
  --property "key.separator=:" \
  <<< '1:{"eventId":"550e8400...","reservationId":1,...}'

# 첫 번째 처리:
# "Kafka 메시지 수신: eventId=550e8400..."
# "데이터 플랫폼 전송 완료: eventId=550e8400..."
# "processed_events에 저장 (SUCCESS)"

# 두 번째 처리 (같은 메시지):
# "Kafka 메시지 수신: eventId=550e8400..."
# "이미 처리된 이벤트 - 스킵: eventId=550e8400..."  ← 멱등성!
# "데이터 플랫폼 호출 0회추가"
```

---

## 개선 사례 vs 기초 사례 비교

| 관점 | 기초 사례 | 개선 사례 |
|------|---------|---------|
| **커밋 보장** | 트랜잭션 중간 발행 (정합성 취약) | TransactionSynchronizationManager (정합성 보장) |
| **Topic 명** | concert.reservation.completed | concert.reservation.completed.v1 (버전 포함) |
| **Key 전략** | Optional | reservationId 고정 (순서/병렬성 균형) |
| **멱등성** | 없음 (중복 처리 가능) | eventId 기반 (processed_events 테이블) |
| **실패 처리** | 로그만 기록 | DLQ + 상세 로깅 + DB 기록 |
| **Offset 관리** | 기본값 (자동) | 기본값 (향후 수동 가능) |
| **모니터링** | 기본 로그 | eventId 기반 추적 가능 |

---

## 코드 구조

```
src/main/java/.../
├── config/
│   └── KafkaConfig.java                    [공통]
│
├── infrastructure/kafka/
│   ├── ReservationEventMessage.java        [공통] 메시지 DTO
│   ├── ReservationEventProducer.java       [개선] TransactionSynchronizationManager
│   └── ReservationEventConsumer.java       [개선] 멱등성 + DLQ
│
├── infrastructure/reservation/
│   ├── ProcessedEventEntity.java           [신규] 멱등성 기록용
│   └── ProcessedEventJpaRepository.java    [신규] 멱등성 조회용
│
└── application/reservation/
    └── ConfirmReservationInteractor.java   [개선] publishReservationCompletedAfterCommit() 호출
```

---

## 주요 학습 포인트

### 1. 트랜잭션 경계와 외부 시스템

**기초**: "언제 외부 시스템을 호출할까?"
- ❌ 트랜잭션 중간 (정합성 깨짐)
- ✅ 트랜잭션 후 (정합성 보장)

### 2. Message Broker의 역할

**역할 1**: 시간 디커플링
- Sender와 receiver가 동시에 실행될 필요 없음

**역할 2**: 공간 디커플링
- Sender와 receiver가 다른 프로세스/서버에 있어도 됨

**역할 3**: 장애 격리
- Receiver 장애가 Sender에 영향 없음

### 3. Partition의 의미

- **순서 보장**: 같은 key의 메시지는 같은 partition → 순서 유지
- **병렬성**: 다른 partition은 다른 consumer가 처리 → 확장성

### 4. 멱등성의 важность

at-least-once 환경에서 **정확히 한 번** 처리를 보장:
- DB에 중복 기록 방지
- 결제 중복 방지
- 감사 추적 정확성

### 5. 버전 관리

Topic에 버전 포함:
- v1 → v2로 점진적 마이그레이션
- 무중단 배포 가능

---

## FAQ

**Q: TransactionSynchronizationManager vs @TransactionalEventListener 중 어떤 것이 나을까?**

A: 둘 다 가능하지만:
- **@TransactionalEventListener**: Spring 이벤트 인프라 활용, 직관적
- **TransactionSynchronizationManager**: 낮은 수준, 더 명시적, Kafka 발행에 꼭 맞음

이 예제는 후자를 선택했습니다 (더 명확한 의도 전달).

**Q: processed_events 테이블이 없으면 멱등성을 어떻게 보장할까?**

A: 비추천. at-least-once 환경에서 테이블 없이는 중복 방지 불가능.
최소 eventId 저장이 필수.

**Q: Kafka 대신 Redis를 쓸 수 있을까?**

A: Redis는 메시지 저장이 아닌 캐시 용도. Kafka와는 용도가 다름:
- Redis: 빠른 응답, 휘발성
- Kafka: 안정성, 감사 추적, 영속성

**Q: 왜 reservationId를 key로 고정했을까?**

A: 
- 같은 예약의 메시지는 같은 partition에 들어감
- Partition 내에서 순서 보장
- 콘서트 ID로 할 수도 있지만, 예약 단위가 더 의미 있음

**Q: Offset 커밋은 언제 이루어질까?**

A: 기본값 (auto commit):
```
Consumer 처리 완료
    ↓ (autocommit interval, 기본 5초)
Offset 자동 커밋
    ↓
다시 시작하면 그 다음부터 읽음
```

---

## 결론

STEP9 개선 사례는 다음을 강조합니다:

1. ✅ **정합성**: TransactionSynchronizationManager로 commit-after-send 보장
2. ✅ **확장성**: Partition/key 설계로 병렬 처리 가능
3. ✅ **신뢰성**: eventId 기반 멱등성으로 at-least-once 대응
4. ✅ **추적성**: 상세 로깅 + DLQ로 실패 대응
5. ✅ **유지보수성**: 버전 포함 topic 명명

프로덕션 환경에는 이정도 수준의 고려까지 필요할 수 있습니다.

---

## 참고 자료

- [TransactionSynchronization API](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/support/TransactionSynchronization.html)
