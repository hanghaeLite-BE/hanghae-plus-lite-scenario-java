-- 재고 동시성 테스트용 데이터

-- TC-STK-001: 재고 1개 상품 동시 주문 테스트용

-- 사용자 A (잔액 100,000원)
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at)
VALUES (4001, 'stock-user-a@example.com', 'password123', 'GENERAL', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO member_balance (id, member_id, balance, created_at, updated_at)
VALUES (4001, 4001, 100000, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

-- 사용자 B (잔액 100,000원)
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at)
VALUES (4002, 'stock-user-b@example.com', 'password123', 'GENERAL', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO member_balance (id, member_id, balance, created_at, updated_at)
VALUES (4002, 4002, 100000, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

-- 재고 1개인 상품 (TC-STK-001용)
INSERT INTO product_summary (id, name, price, stock_quantity, created_at, updated_at)
VALUES (5001, '재고 1개 테스트 상품', 10000, 1, '2025-01-01 00:00:00', '2025-01-01 00:00:00');


-- TC-STK-002: 재고 5개 상품 동시 대량 주문 테스트용

-- 사용자 C ~ G (각각 잔액 100,000원)
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at)
VALUES
    (4003, 'stock-user-c@example.com', 'password123', 'GENERAL', '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (4004, 'stock-user-d@example.com', 'password123', 'GENERAL', '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (4005, 'stock-user-e@example.com', 'password123', 'GENERAL', '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (4006, 'stock-user-f@example.com', 'password123', 'GENERAL', '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (4007, 'stock-user-g@example.com', 'password123', 'GENERAL', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO member_balance (id, member_id, balance, created_at, updated_at)
VALUES
    (4003, 4003, 100000, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (4004, 4004, 100000, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (4005, 4005, 100000, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (4006, 4006, 100000, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (4007, 4007, 100000, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

-- 재고 5개인 상품 (TC-STK-002용)
INSERT INTO product_summary (id, name, price, stock_quantity, created_at, updated_at)
VALUES (5002, '재고 5개 테스트 상품', 10000, 5, '2025-01-01 00:00:00', '2025-01-01 00:00:00');
