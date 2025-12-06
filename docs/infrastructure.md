```
kr.hhplus.be.server
├── domain/
│   ├── model/
│   │   ├── SeatReservation.java
│   │   ├── UserBalance.java
│   │   ├── Payment.java
│   │   ├── OutboxEvent.java
│   │   ├── BalanceHistory.java
│   │   └── enums/
│   │       ├── ReservationStatus.java
│   │       ├── PaymentStatus.java
│   │       ├── TransactionType.java
│   │       └── OutboxEventStatus.java
│   └── repository/
│       ├── SeatReservationRepository.java
│       ├── UserBalanceRepository.java
│       └── PaymentRepository.java
│
└── infrastructure/
├── persistence/
│   ├── entity/
│   │   ├── SeatReservationEntity.java
│   │   ├── UserBalanceEntity.java
│   │   ├── PaymentEntity.java
│   │   ├── OutboxEventEntity.java
│   │   └── BalanceHistoryEntity.java
│   │
│   ├── SeatReservationJpaRepository.java
│   ├── UserBalanceJpaRepository.java
│   ├── PaymentJpaRepository.java
│   ├── OutboxEventJpaRepository.java
│   ├── BalanceHistoryJpaRepository.java
│   │
│   ├── SeatReservationRepositoryImpl.java
│   ├── UserBalanceRepositoryImpl.java
│   └── PaymentRepositoryImpl.java
│
├── configuration/
│   ├── JpaConfig.java
│   ├── DatabaseConfig.java
│   ├── CacheConfig.java
│   └── SchedulingConfig.java
│
├── external/
│   ├── MessageQueueProducer.java
│   ├── MockMessageQueueProducer.java
│   ├── KafkaMessageQueueProducer.java
│   └── ExternalApiClient.java
│
├── scheduler/
│   ├── OutboxEventProcessor.java
│   ├── ExpiredDataCleanupScheduler.java
│   └── StatisticsUpdateScheduler.java
│
├── monitoring/
│   ├── DatabaseMetrics.java
│   ├── HealthCheckService.java
│   └── AuditLogService.java
│
├── security/
│   ├── EncryptionService.java
│   ├── DataMaskingService.java
│   └── AccessControlService.java
│
└── cache/
├── CacheKeyGenerator.java
├── CacheService.java
└── RedisCacheService.java
```