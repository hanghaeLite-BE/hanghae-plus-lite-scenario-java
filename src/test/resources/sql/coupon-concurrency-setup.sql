-- 쿠폰 동시성 테스트용 데이터

-- TC-CPN-001: 선착순 쿠폰 수량 초과 발급 검증
-- 시나리오: 2명의 사용자가 동시에 maxCount=1인 쿠폰 발급 요청

-- 사용자 A (쿠폰 발급 요청 사용자 1)
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at)
VALUES (5001, 'coupon-user-a@example.com', 'password123', 'GENERAL', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO member_balance (id, member_id, balance, created_at, updated_at)
VALUES (5001, 5001, 100000, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

-- 사용자 B (쿠폰 발급 요청 사용자 2)
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at)
VALUES (5002, 'coupon-user-b@example.com', 'password123', 'GENERAL', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO member_balance (id, member_id, balance, created_at, updated_at)
VALUES (5002, 5002, 100000, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

-- 선착순 쿠폰 정보 (maxCount=1: 1명만 발급 가능)
INSERT INTO coupon_summary (id, name, discount_percentage, valid_days, created_at, updated_at)
VALUES (6001, '선착순 1명 한정 쿠폰', 10, 30, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO coupon_issuance (id, coupon_summary_id, issued_count, max_count, start_at, end_at, created_at, updated_at)
VALUES (6001, 6001, 0, 1, '2025-01-01 00:00:00', '2025-12-31 23:59:59', '2025-01-01 00:00:00', '2025-01-01 00:00:00');


-- TC-CPN-002: 동일 회원 쿠폰 중복 발급 검증 (OnePerMember 정책)
-- 시나리오: 동일 사용자가 동시에 2번의 쿠폰 발급 요청 (OnePerMember 정책 적용)

-- 사용자 C (중복 발급 테스트용)
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at)
VALUES (5003, 'coupon-user-c@example.com', 'password123', 'GENERAL', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO member_balance (id, member_id, balance, created_at, updated_at)
VALUES (5003, 5003, 100000, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

-- OnePerMember 정책 쿠폰 정보 (회원당 1번만 발급 가능)
INSERT INTO coupon_summary (id, name, discount_percentage, valid_days, created_at, updated_at)
VALUES (6002, '회원당 1회 한정 쿠폰', 20, 30, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO coupon_issuance (id, coupon_summary_id, issued_count, max_count, start_at, end_at, created_at, updated_at)
VALUES (6002, 6002, 0, 100, '2025-01-01 00:00:00', '2025-12-31 23:59:59', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

-- OnePerMember 정책 추가
INSERT INTO coupon_issuance_policy (id, coupon_issuance_id, policy_type, created_at, updated_at)
VALUES (6002, 6002, 'ONE_PER_MEMBER', '2025-01-01 00:00:00', '2025-01-01 00:00:00');


-- TC-CPN-003: 동일 쿠폰 동시 사용 검증 (주문 시)
-- 시나리오: 같은 회원이 동일한 쿠폰으로 동시에 2건의 주문 요청

-- 사용자 D (쿠폰 사용 테스트용, 잔액 100,000원)
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at)
VALUES (5004, 'coupon-user-d@example.com', 'password123', 'GENERAL', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO member_balance (id, member_id, balance, created_at, updated_at)
VALUES (5004, 5004, 100000, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

-- 쿠폰 정보 (10% 할인)
INSERT INTO coupon_summary (id, name, discount_percentage, valid_days, created_at, updated_at)
VALUES (6003, '10% 할인 쿠폰', 10, 30, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO coupon_issuance (id, coupon_summary_id, issued_count, max_count, start_at, end_at, created_at, updated_at)
VALUES (6003, 6003, 1, 100, '2025-01-01 00:00:00', '2025-12-31 23:59:59', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

-- 사용자 D에게 발급된 쿠폰 (미사용 상태, usingAt = NULL)
INSERT INTO coupon (id, coupon_summary_id, member_id, expired_at, using_at, created_at, updated_at)
VALUES (6003, 6003, 5004, '2025-12-31 23:59:59', NULL, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

-- 주문용 상품 (재고 충분: 100개)
INSERT INTO product_summary (id, name, price, stock_quantity, created_at, updated_at)
VALUES (6001, '쿠폰 테스트 상품', 10000, 100, '2025-01-01 00:00:00', '2025-01-01 00:00:00');
