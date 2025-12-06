-- 회원 데이터 생성
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at)
VALUES (2004, 'concurrency-user@example.com', 'password123', 'GENERAL', '2025-01-02 00:00:00', '2025-01-18 13:20:12');

-- 회원 잔액 데이터 생성
INSERT INTO member_balance (id, member_id, balance, created_at, updated_at)
VALUES (2004, 2004, 3000000, '2025-09-30 00:00:00', '2025-09-30 00:00:00');

-- 쿠폰 요약 정보 생성
INSERT INTO coupon_summary (id, name, discount_percentage, valid_days, created_at, updated_at)
VALUES (200, '동시성 테스트 쿠폰', 10, 365, '2025-09-30 00:00:00', '2025-09-30 00:00:00');

-- 회원이 소유한 쿠폰 생성
INSERT INTO coupon (id, coupon_summary_id, member_id, using_at, expired_at, created_at, updated_at)
VALUES (2000, 200, 2004, NULL, '2030-12-31 23:59:59', '2025-09-30 00:00:00', '2025-09-30 00:00:00');

-- 상품 요약 정보 생성
INSERT INTO product_summary (id, name, price, stock_quantity, created_at, updated_at)
VALUES (9001, '동시성 테스트 상품 A', 1790000, 50, '2025-09-30 00:00:00', '2025-09-30 00:00:00'),
       (9002, '동시성 테스트 상품 B', 99000, 50, '2025-09-30 00:00:00', '2025-09-30 00:00:00');
