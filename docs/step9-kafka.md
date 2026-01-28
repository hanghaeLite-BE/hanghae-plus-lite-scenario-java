# STEP 9: Kafka (일반 사례 - 기초 수준)

## 개요
예약 확정 시 "실시간 예약정보(콘서트)"를 **Kafka**를 통해 **데이터 플랫폼에 전송**하는 요구사항을 구현한다.

이전 단계(STEP8)는 Spring Application Event로 프로세스 **내부**에서만 이벤트를 처리했다면,
STEP9에서는 Kafka 메시지 브로커를 활용하여 **프로세스 외부**로 메시지를 발행한다.

---

## 핵심 개념: 왜 Kafka인가?

### 1. Event 아이디어의 확장
```
[STEP8: Application Event 내부 처리]
Interactor (이벤트 발행)
    ↓
@TransactionalEventListener (같은 프로세스 내)
    ↓
DataPlatformClient (프로세스 종료 후)

문제: 이벤트가 프로세스 내부에만 존재 → 다른 서비스가 구독 불가


[STEP9: Kafka 외부 처리]
Interactor (메시지 발행)
    ↓
Kafka Broker (프로세스 외부, 분산 스토리지)
    ↓
Consumer (같은 프로세스 내 또는 다른 서비스)
    ↓
DataPlatformClient

장점: 메시지가 영속적으로 저장 → 여러 서비스가 구독 가능
```

### 2. MQ vs Event vs Kafka

| 개념 | 저장소 | 확장성 | 사용 시점 |
|------|--------|--------|---------|
| **MQ** (ActiveMQ, RabbitMQ) | 메모리 기반 | 중간 | 큐 방식의 간단한 작업 큐 |
| **Event** (ApplicationEvent) | 프로세스 메모리 | 낮음 | 같은 프로세스 내 느슨한 결합 |
| **Event Stream** (Kafka) | 분산 디스크 | 높음 | 마이크로서비스 간 신뢰할 수 있는 전송 |

---

## Kafka 구성 요소

### 1. Broker (브로커)
- Kafka 서버 (메시지 저장/전달 담당)
- 로컬 클러스터: 3개 브로커 (포트 9092, 9093, 9094)
- Zookeeper: 브로커 상태 관리

### 2. Topic (토픽)
- 메시지의 카테고리 (예: "concert.reservation.completed")
- 여러 Consumer가 구독 가능
- 같은 Topic의 메시지는 여러 Consumer에게 전달됨

### 3. Partition (파티션)
- Topic을 분할한 단위 (병렬 처리)
- 같은 key의 메시지는 같은 partition으로 발행
- Consumer Group이 partition을 분담하여 처리

예시:
```
Topic: concert.reservation.completed
Partition 0: reservationId=1, 3, 5 (key=reservationId)
Partition 1: reservationId=2, 4, 6
             ↓
Consumer Group에 2개 Consumer:
- Consumer 1: Partition 0 처리
- Consumer 2: Partition 1 처리
```

### 4. Offset (오프셋)
- 메시지의 순서 번호 (0부터 시작)
- Consumer가 "어디까지 처리했는지" 추적
- 재시작 시 AUTO_OFFSET_RESET: "earliest" → 가장 오래된 메시지부터 시작

### 5. Consumer Group (컨슈머 그룹)
- 같은 Topic을 처리하는 Consumer들의 집합
- 그룹 내에서는 파티션을 나누어 처리 (중복 방지)
- 그룹 외에서는 독립적인 offset 관리

예시:
```
Consumer Group A: concert-ranking-consumer-group
└─ Consumer 1 (partition 0 처리)
└─ Consumer 2 (partition 1 처리)

Consumer Group B: analytics-consumer-group
└─ Consumer 3 (전체 처리, offset 독립 관리)
```

---

## 로컬 환경 설정

### 1. Docker Compose로 Kafka 클러스터 실행

```bash
# 터미널에서 프로젝트 루트로 이동
cd /home/andrew/lecture/hanghae-plus-lite-scenario-java

# Kafka 클러스터 시작
docker-compose -f docker-compose.kafka.yaml up -d

# 상태 확인
docker-compose -f docker-compose.kafka.yaml ps

# 로그 확인
docker-compose -f docker-compose.kafka.yaml logs -f
```

### 2. 클러스터 구성
```yaml
- Zookeeper: 2181 포트
- Broker 1: localhost:9092
- Broker 2: localhost:9093
- Broker 3: localhost:9094
```

### 3. 종료
```bash
docker-compose -f docker-compose.kafka.yaml down
```

---

## 콘서트 예약 서비스 적용 흐름

### 아키텍처
```
[클라이언트]
    ↓
[ReservationController.confirmReservation()]
    ↓
[ConfirmReservationInteractor.confirm()] @Transactional
    ├─ 1. 포인트 차감 (DB)
    ├─ 2. 예약 상태 변경 (DB)
    ├─ 3. 결제 기록 생성 (DB)
    ├─ 4. Kafka 메시지 발행 ← [NEW]
    │   └─ Topic: concert.reservation.completed
    │   └─ ReservationEventMessage (JSON)
    └─ 5. 랭킹 정보 업데이트 (Redis)
    
    ║ 트랜잭션 commit
    ↓
    
[ReservationEventConsumer] @KafkaListener
    ├─ Kafka에서 메시지 수신
    ├─ DataPlatform 호출
    └─ 로그 기록
```

### 메시지 포맷

**Topic**: `concert.reservation.completed`

**Key**: (Optional) `reservationId` (같은 예약의 메시지는 같은 partition으로)

**Value** (JSON):
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "reservationId": 12345,
  "userId": 1,
  "concertId": 5,
  "seatId": 42,
  "paidAmount": 150000,
  "occurredAt": "2026-01-28T01:35:00"
}
```

---

## 구현 내용

### 1. Kafka 설정 (KafkaConfig.java)

```java
@Configuration
@EnableKafka
public class KafkaConfig {
    
    // Producer 설정
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() { ... }
    
    // Consumer 설정
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> 
           kafkaListenerContainerFactory() { ... }
}
```

**특징**:
- Bootstrap Servers: 3개 브로커 (9092, 9093, 9094)
- Serializer: JSON (spring-kafka 기본)
- Consumer Group: "concert-reservation-consumer-group"
- auto.offset.reset: "earliest" (초기 offset은 가장 오래된 메시지)

### 2. 메시지 DTO (ReservationEventMessage.java)

```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEventMessage {
    String eventId;        // UUID - 추적용
    Long reservationId;
    Long userId;
    Long concertId;
    Long seatId;
    Long paidAmount;
    String occurredAt;     // ISO-8601
}
```

### 3. Producer (ReservationEventProducer.java)

```java
@Component
public class ReservationEventProducer {
    
    public static final String TOPIC = "concert.reservation.completed";
    
    public void publishReservationCompleted(ReservationEventMessage message) {
        try {
            String key = message.getReservationId().toString();
            kafkaTemplate.send(TOPIC, key, message);
            log.info("Kafka 메시지 발행: eventId={}", message.getEventId());
        } catch (Exception e) {
            log.error("Kafka 발행 실패: eventId={}", message.getEventId(), e);
            throw e;  // 기초 수준: 예외 재발생
        }
    }
}
```

**특징**:
- Topic: "concert.reservation.completed"
- Key: reservationId (같은 예약은 같은 partition)
- Fire-and-forget (callback 없음)
- 실패 시 예외 발생

### 4. Consumer (ReservationEventConsumer.java)

```java
@Component
public class ReservationEventConsumer {
    
    @KafkaListener(topics = ReservationEventProducer.TOPIC, 
                   groupId = "concert-reservation-consumer-group")
    public void handleReservationCompleted(ReservationEventMessage message) {
        try {
            log.info("Kafka 메시지 수신: eventId={}", message.getEventId());
            
            // 데이터 플랫폼으로 전송
            DataPlatformClient.ReservationEventPayload payload = 
                    DataPlatformClient.ReservationEventPayload.builder()
                            .eventId(message.getEventId())
                            // ... 다른 필드
                            .build();
            dataPlatformClient.postReservationEvent(payload);
            
            log.info("데이터 플랫폼 전송 완료: eventId={}", message.getEventId());
        } catch (Exception e) {
            log.error("데이터 플랫폼 전송 실패: eventId={}", 
                    message.getEventId(), e);
            // 기초 수준: 로그만 기록 (재시도 없음)
        }
    }
}
```

**특징**:
- Topic 구독: "concert.reservation.completed"
- Consumer Group: "concert-reservation-consumer-group"
- 실패 시 로그 기록만 수행 (예외 발생 안 함)

### 5. Interactor 수정 (ConfirmReservationInteractor.java)

```java
@Transactional
public void confirm(Command command) {
    // ... 포인트 차감, 예약 상태 변경, 결제 기록
    
    // [NEW] Kafka로 메시지 발행
    String eventId = UUID.randomUUID().toString();
    ReservationEventMessage kafkaMessage = ReservationEventMessage.builder()
            .eventId(eventId)
            .reservationId(reservation.getId())
            .userId(member.getId())
            .concertId(seat.getConcertId())
            .seatId(seat.getId())
            .paidAmount(seat.getPrice())
            .occurredAt(LocalDateTime.now().toString())
            .build();
    
    reservationEventProducer.publishReservationCompleted(kafkaMessage);
    
    // ... 랭킹 정보 업데이트
}
```

---

## 기초 수준 vs 개선 사례 비교

| 관점 | 기초 수준 (현재) | 개선 사례 (다음) |
|------|---------|---------|
| **메시지 보장** | Fire-and-forget | Transactional Outbox |
| **재시도** | 없음 | ExponentialBackoff |
| **실패 처리** | 로그 기록만 | Dead Letter Queue |
| **멱등성** | 고려 안 함 | Consumer에서 중복 제거 |
| **Schema** | JSON 수동 | Avro + Schema Registry |
| **모니터링** | 로그만 | Metrics + Alert |

---

## 테스트 시나리오

### 1. 기본 흐름 검증
```bash
# Terminal 1: Kafka 클러스터 시작
docker-compose -f docker-compose.kafka.yaml up -d

# Terminal 2: 애플리케이션 실행
./gradlew bootRun

# Terminal 3: 예약 확정 호출
curl -X POST http://localhost:8080/api/reservations/{id}/confirm \
  -H "Content-Type: application/json" \
  -d '{"userId": 1}'
```

### 2. 로그 확인
```
# Producer 로그:
Kafka 메시지 발행 시작: eventId=550e8400-e29b-41d4-a716-446655440000, topic=concert.reservation.completed
Kafka 메시지 발행 완료: eventId=550e8400-e29b-41d4-a716-446655440000, reservationId=1

# Consumer 로그:
Kafka 메시지 수신: eventId=550e8400-e29b-41d4-a716-446655440000, reservationId=1
데이터 플랫폼 전송 완료: eventId=550e8400-e29b-41d4-a716-446655440000, reservationId=1
```

### 3. Kafka CLI로 메시지 확인 (선택사항)
```bash
# 토픽 생성 여부 확인
docker exec broker1 kafka-topics --bootstrap-server localhost:9092 --list

# 메시지 조회
docker exec broker1 kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic concert.reservation.completed \
  --from-beginning
```

---

## 파일 구조

```
src/main/java/kr/hhplus/be/server/
├── config/
│   └── KafkaConfig.java                    [신규] Kafka 설정
│
├── infrastructure/
│   ├── kafka/
│   │   ├── ReservationEventMessage.java    [신규] 메시지 DTO
│   │   ├── ReservationEventProducer.java   [신규] Producer
│   │   └── ReservationEventConsumer.java   [신규] Consumer
│   │
│   └── external/
│       └── DataPlatformClient.java         [기존] mock API 클라이언트
│
└── application/
    └── reservation/
        └── ConfirmReservationInteractor.java [수정] Kafka 발행 추가

docker-compose.kafka.yaml                   [신규] Kafka 클러스터 정의
docs/step9-kafka.md                         [신규] 본 문서
```

---

## 주요 학습 포인트

### 1. 분산 시스템의 이벤트 처리
- 프로세스 내부 이벤트 (Application Event) vs 외부 메시지 (Kafka)
- 메시지 브로커의 역할: 시간/공간 디커플링

### 2. Kafka 기본 개념
- Topic, Partition, Offset, Consumer Group의 이해
- 메시지 순서 보장 (같은 key → 같은 partition)
- Stateless Consumer (offset으로 진행 상황 추적)

### 3. 기초 vs 개선
- 현재: 동작하지만 개선 포인트 많음 (Fire-and-forget, 재시도 없음)
- 개선 사례: Exactly-once, Dead Letter Queue, Transactional Outbox 등
---

## FAQ

**Q: Producer 발행 후 즉시 응답해도 되나?**
A: 네. 기초 수준에서는 fire-and-forget. Consumer 처리를 기다리지 않음.
개선 사례에서는 "commit이후 발행 보장" (Outbox) 추가.

**Q: Consumer 처리 실패 시?**
A: 기초 수준은 로그만 기록. 개선 사례에서는 Dead Letter Queue 구현.

**Q: Kafka 대신 RabbitMQ 사용하면?**
A: RabbitMQ도 MQ지만 Kafka 대비:
- 메시지 히스토리 보존 안 됨 (소비 후 삭제)
- 재전송 어려움
- 따라서 "이벤트 스트리밍"에는 부적합

**Q: 멱등성은 왜 중요한가?**
A: Consumer가 같은 메시지를 2번 처리할 수 있음 (네트워크 재시도).

**Q: Exactly-once는?**
A: "정확히 한 번" 전송 보장. Transactional Producer + Transactional Consumer.

---

## 참고 자료

- [Apache Kafka 공식 문서](https://kafka.apache.org/documentation/)
- [Spring for Apache Kafka](https://spring.io/projects/spring-kafka)
