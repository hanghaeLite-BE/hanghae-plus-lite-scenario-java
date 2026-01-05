-- 분산락 동시성 테스트용 데이터

-- TC-LOCK-001: 재고 1개 상품 대량 동시 주문 테스트용

-- 사용자 5001 ~ 5100 (각각 잔액 100,000원)
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at)
SELECT 
    5000 + n,
    CONCAT('lock-test-user-', n, '@example.com'),
    'password123',
    'GENERAL',
    '2025-01-01 00:00:00',
    '2025-01-01 00:00:00'
FROM (
    SELECT @row := @row + 1 AS n
    FROM (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1,
         (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2,
         (SELECT @row := 0) r
    LIMIT 100
) numbers;

INSERT INTO member_balance (id, member_id, balance, created_at, updated_at)
SELECT 
    5000 + n,
    5000 + n,
    100000,
    '2025-01-01 00:00:00',
    '2025-01-01 00:00:00'
FROM (
    SELECT @row := @row + 1 AS n
    FROM (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1,
         (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2,
         (SELECT @row := 0) r
    LIMIT 100
) numbers;

-- 재고 1개인 상품 (TC-LOCK-001용)
INSERT INTO product_summary (id, name, price, stock_quantity, created_at, updated_at)
VALUES (6001, '재고 1개 분산락 테스트 상품', 10000, 1, '2025-01-01 00:00:00', '2025-01-01 00:00:00');


-- TC-LOCK-002: 재고 10개 상품 대량 동시 주문 테스트용

-- 사용자 5201 ~ 5220 (각각 잔액 100,000원)
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at)
SELECT 
    5200 + n,
    CONCAT('lock-test-user-', n, '@example.com'),
    'password123',
    'GENERAL',
    '2025-01-01 00:00:00',
    '2025-01-01 00:00:00'
FROM (
    SELECT @row := @row + 1 AS n
    FROM (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1,
         (SELECT 0 UNION SELECT 1 UNION SELECT 2) t2,
         (SELECT @row := 0) r
    LIMIT 20
) numbers;

INSERT INTO member_balance (id, member_id, balance, created_at, updated_at)
SELECT 
    5200 + n,
    5200 + n,
    100000,
    '2025-01-01 00:00:00',
    '2025-01-01 00:00:00'
FROM (
    SELECT @row := @row + 1 AS n
    FROM (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1,
         (SELECT 0 UNION SELECT 1 UNION SELECT 2) t2,
         (SELECT @row := 0) r
    LIMIT 20
) numbers;

-- 재고 10개인 상품 (TC-LOCK-002용)
INSERT INTO product_summary (id, name, price, stock_quantity, created_at, updated_at)
VALUES (6002, '재고 10개 분산락 테스트 상품', 10000, 10, '2025-01-01 00:00:00', '2025-01-01 00:00:00');
