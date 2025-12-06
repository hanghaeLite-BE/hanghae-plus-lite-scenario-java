-- 회원 데이터 생성
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at)
VALUES (4, 'njz@gmail.com', 'securepassword', 'GENERAL', '2025-01-02 00:00:00', '2025-01-18 13:20:12');

-- 회원의 잔액 데이터 생성
INSERT INTO member_balance (id, member_id, balance, created_at, updated_at)
VALUES (4, 4, 2000000, '2025-01-02 00:00:00', '2025-03-18 13:00:00');

-- 쿠폰 데이터 생성
INSERT INTO coupon_summary (id, name, discount_percentage, valid_days, created_at, updated_at)
VALUES (1, '10% 할인 쿠폰', 10, 90, '2025-03-18 13:00:00', '2025-03-18 13:00:00');

-- member_id=4인 회원이 쿠폰을 소유한 상태
INSERT INTO coupon (id, coupon_summary_id, member_id, using_at, expired_at, created_at, updated_at)
VALUES (1, 1, 4, NULL, '2025-06-18 13:00:00', '2025-03-18 13:00:00', '2025-03-20 13:00:00');

-- 상품 데이터 생성
INSERT INTO product_summary (id, name, price, stock_quantity, created_at, updated_at)
VALUES (1, '아이폰 17 pro', 1790000, 10, '2025-03-18 13:00:00', '2025-03-18 13:00:00'),
       (2, '케스티파이 케이스', 89000, 20, '2025-03-18 13:00:00', '2025-03-18 13:00:00'),
       (3, '신지모루 액정필름', 12000, 100, '2025-03-18 13:00:00', '2025-03-18 13:00:00');


