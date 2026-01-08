# STEP 7: Redis 기반 랭킹 서비스 디자인 보고서

## 1. 개요
콘서트 예약 시스템의 핵심 지표인 "빠른 매진 랭킹"을 제공하기 위해, Redis의 Sorted Set과 Hash 자료구조를 활용하여 효율적이고 정합성 있는 랭킹 시스템을 재설계하였습니다. 
특히 Lua 스크립트 없이도 Java 수준의 원자적 연산(`setIfAbsent`)과 DB Source of Truth 전략을 조합하여 기술적 부채를 해결했습니다.

## 2. 시스템 디자인

### 랭킹 지표의 재정의
- **지표명**: 빠른 매진 소요 시간 (Sold-out Duration)
- **수식**: `soldOutDuration = soldOutAt - salesStartAt`
  - `salesStartAt`: 해당 콘서트의 판매가 시작된 최초 시점 (최초 결제 확정 시각으로 대체 기록 가능)
  - `soldOutAt`: 모든 좌석이 '결제 확정' 상태가 된 시점
- **순위 산정**: 소요 시간이 짧을수록(낮을수록) 높은 순위를 차지합니다.

### 자료구조 및 키 설계
1. **랭킹 보관 (Sorted Set)**
   - Key: `ranking:soldout_speed`
   - Member: `concertId`
   - Score: `durationMillis`
   - TTL: 30일 (비즈니스 요구사항에 따라 조절 가능)

2. **판매 상태 관리 (Hash & Strings)**
   - Hash Key: `concert:{concertId}:sales`
     - Fields: `confirmedCount`, `totalSeats`
   - String Key (판매 시작): `concert:{concertId}:sales:salesStartAt`
   - String Key (매진 시점): `concert:{concertId}:sales:soldOutAt`
   - TTL: 판매 종료 후 7일 (메모리 효율화)

## 3. 핵심 개선 포인트

### (1) Lua 스크립트 없는 원자성 및 정합성 보장
- **SET NX (setIfAbsent) 활용**: `salesStartAt`과 `soldOutAt` 기록 시 `setIfAbsent`를 사용하여 최초 1회만 기록됨을 보장합니다.
- **랭킹 등록 조건화**: `soldOutAt` 기록에 성공한 요청만 랭킹(`ZADD`)을 수행하도록 설계하여, 동시 결제 상황에서도 랭킹 업데이트가 중복 발생하지 않도록 제어했습니다.
- **DB Source of Truth**: Redis의 고장이나 데이터 유실을 대비하여, 결제 확정 시마다 DB에서 실제 확정 판매 좌석수를 조회하여 Redis에 반영함으로써 최종적인 데이터 정합성을 DB에 둡니다.

### (2) 계층 구조 및 책임 분리 (Clean Architecture)
- **Port/Adapter 패턴**: `ConcertRankingPort`를 정의하고 인프라 레이어의 `RedisRankingAdapter`에서 구현함으로써, 비즈니스 로직이 Redis 구체 기술에 의존하지 않도록 격리했습니다.
- **결제 확정 연계**: 예약 선점 시점이 아닌, 실제 매출이 발생하는 '결제 확정' 시점에 랭킹을 집계하도록 로직을 이동했습니다.

### (3) 유지보수성 향상
- Lua 스크립트를 배제하고 표준 Redis 명령과 순수 Java 코드로 구현하여, 팀원 누구나 서비스의 흐름을 쉽게 파악하고 유지보수할 수 있는 "팀 친화적 구조"를 채택했습니다.

## 4. 테스트 결과
- **멀티스레드 동시성 테스트**: 10개의 스레드가 동시에 매진 시점을 기록하려고 시도했을 때, `soldOutAt` 기록 및 랭킹 등록이 정확히 1회만 발생하는 것을 확인했습니다.
- **데이터 일관성**: 결제 확정 수에 비례하여 Redis의 `confirmedCount`가 정확히 업데이트됨을 검증했습니다.

## 5. 회고 (아쉬운 사례 대비 개선점)
- **기존 문제**: 모호한 지표(예약 수), TTL 부재로 인한 메모리 누수 위험, 레이어 침범, 원자성 부족.
- **개선 결과**: 명확한 시간 기반 지표 도입, 적절한 TTL 정책 수립, 계층 간 의존성 정립, `setIfAbsent` 기반의 안전한 원자적 갱신 확보.
