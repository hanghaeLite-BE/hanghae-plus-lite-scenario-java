-- 쿠폰 요약(정책별, 기간별, 수량별 테스트용)
INSERT INTO coupon_summary (id, name, discount_percentage, valid_days, created_at, updated_at) VALUES
(1, '정상쿠폰', 10, 10, '2025-09-24 00:00:00', '2025-09-24 00:00:00'),
(2, '기간외쿠폰', 20, 10, '2025-09-24 00:00:00', '2025-09-24 00:00:00'),
(3, '수량소진쿠폰', 30, 10, '2025-09-24 00:00:00', '2025-09-24 00:00:00');

-- 쿠폰 발급 상태(정상, 기간외, 수량소진)
INSERT INTO coupon_issuance (id, coupon_summary_id, issued_count, max_count, start_at, end_at, created_at, updated_at) VALUES
(1, 1, 0, 100, '2025-09-20 00:00:00', '2025-09-30 23:59:59', '2025-09-24 00:00:00', '2025-09-24 00:00:00'), -- 정상
(2, 2, 0, 100, '2025-08-01 00:00:00', '2025-08-31 23:59:59', '2025-09-24 00:00:00', '2025-09-24 00:00:00'), -- 기간외
(3, 3, 100, 100, '2025-09-20 00:00:00', '2025-09-30 23:59:59', '2025-09-24 00:00:00', '2025-09-24 00:00:00'); -- 수량소진

-- 쿠폰발급정책 (ONE_PER_MEMBER)
INSERT INTO coupon_issuance_policy (id, coupon_issuance_id, policy_type, created_at, updated_at) VALUES
(1, 1, 'ONE_PER_MEMBER', '2025-09-24 00:00:00', '2025-09-24 00:00:00');

-- 이미 발급된 쿠폰(정책 위반 테스트용)
INSERT INTO coupon (id, member_id, coupon_summary_id, using_at, expired_at, created_at, updated_at) VALUES
(1, 2, 1, NULL, '2025-09-30 23:59:59', '2025-09-24 00:00:00', '2025-09-24 00:00:00');
