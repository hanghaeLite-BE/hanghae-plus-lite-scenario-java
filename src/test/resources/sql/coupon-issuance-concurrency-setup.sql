-- 동시성 테스트용 회원 데이터
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at) VALUES
  (101, 'concurrency-user1@example.com', 'password1', 'GENERAL', '2025-09-24 00:00:00', '2025-09-24 00:00:00'),
  (102, 'concurrency-user2@example.com', 'password2', 'GENERAL', '2025-09-24 00:00:00', '2025-09-24 00:00:00');

-- 동시성 테스트용 쿠폰 요약
INSERT INTO coupon_summary (id, name, discount_percentage, valid_days, created_at, updated_at) VALUES
  (100, '동시성 테스트 쿠폰', 10, 10, '2025-09-24 00:00:00', '2025-09-24 00:00:00');

-- 발급 가능 수량이 1개인 쿠폰 발급 정보
INSERT INTO coupon_issuance (id, coupon_summary_id, issued_count, max_count, start_at, end_at, created_at, updated_at) VALUES
  (100, 100, 0, 1, '2025-09-20 00:00:00', '2025-09-30 23:59:59', '2025-09-24 00:00:00', '2025-09-24 00:00:00');

-- 회원당 1회 발급 정책
INSERT INTO coupon_issuance_policy (id, coupon_issuance_id, policy_type, created_at, updated_at) VALUES
  (100, 100, 'ONE_PER_MEMBER', '2025-09-24 00:00:00', '2025-09-24 00:00:00');
