# virtual-2-commerce

## 개요
Kotlin 2.1과 Spring Boot 3.4로 구축한 전자상거래 백엔드 애플리케이션입니다. 주문, 결제, 쿠폰, 잔액, 상품 도메인을 중심으로 핵심 커머스 기능을 제공하며 클린 아키텍처 기반의 계층 구조를 유지합니다.

## 주요 기능
- 잔액: 잔액 조회·충전·차감과 같은 지갑 기능 및 부족/비정상 금액 검증을 제공합니다.
- 상품: 정렬/페이징 목록과 최근 N일간 상위 M개 인기상품 API, 재고 차감/증가 로직을 포함합니다.
- 주문/결제: 주문 생성, 결제 요약 생성, 쿠폰 할인 적용, 잔액·재고 차감, 주문 완료 이벤트 발행까지 단일 트랜잭션에서 처리합니다.
- 쿠폰: 발급량·기간·중복 발급·VIP 전용 정책 검증을 지원하며 정책별 전략 클래스로 확장 가능합니다.
- 외부 연동: 주문 완료 이벤트를 외부 데이터 플랫폼 API에 POST하여 데이터 적재를 트리거합니다.

## 시스템 아키텍처
### 애플리케이션 계층 구조
- `presentation → application → domain → infrastructure` 계층을 명확히 분리한 포트/어댑터 구조입니다.
- Application 계층에는 유즈케이스 인터페이스와 인터랙터/파사드가 위치해 도메인 규칙을 조합하고 흐름을 제어합니다.
- Infrastructure 계층은 JPA 기반의 영속성 어댑터, RestTemplate 기반 외부 연동 어댑터 등을 제공합니다.

### 데이터베이스와 인프라
- 멀티 데이터소스 구성을 통해 읽기 전용 트랜잭션은 슬레이브, 쓰기 트랜잭션은 마스터 MySQL로 라우팅합니다.
- `docker-compose.yml`로 MySQL 8.0 마스터/슬레이브 환경을 제공하며 초기 스키마와 샘플 데이터를 자동 로드합니다.
- 문서 폴더(`docs/`)에서 ERD, REST API 명세, Amazon EKS 기반 인프라 구성을 확인할 수 있습니다.

## 기술 스택
- Kotlin 2.1, Java 21
- Spring Boot 3.4 (Web, Data JPA/Redis, Actuator, Kafka)
- MySQL 8.0, Redis 7, JPA/Hibernate, HikariCP
- Kotlin Logging, SpringDoc OpenAPI
- Docker Compose, Testcontainers, MockK

## 시작하기
### 사전 준비
- JDK 21 이상
- Docker 및 Docker Compose
- 로컬 실행 시 `local` 프로파일을 사용합니다.

### 로컬 실행 절차
1. 인프라 컨테이너 기동:
   ```bash
   docker-compose up -d
   ```
   Redis 7과 MySQL 마스터/슬레이브가 함께 기동됩니다.
2. 애플리케이션 실행:
   ```bash
   ./gradlew bootRun
   ```
3. 애플리케이션 종료 후 컨테이너 정리:
   ```bash
   docker compose down
   ```

### 테스트 실행
```bash
./gradlew test
```

## 문서
- [API 명세서](docs/API명세서.md)
- [ERD 문서](docs/ERD-데이터베이스.md)
- [인프라 구성도](docs/인프라구성도.md)
- Swagger UI: 애플리케이션 실행 후 `http://localhost:8080/swagger-ui.html`
