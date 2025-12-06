-- 쿠폰 데이터 생성
INSERT INTO coupon_summary (id, name, discount_percentage, valid_days, created_at, updated_at) VALUES
(1, 'SPRING_SALE', 10, 3, '2025-03-18 13:00:00', '2025-03-18 13:00:00'),
(2, 'SUMMER_SALE', 15, 5, '2025-06-18 13:00:00', '2025-06-18 13:00:00'),
(3, 'FALL_SALE', 20, 7, '2025-09-18 13:00:00', '2025-09-18 13:00:00'),
(4, 'WINTER_SALE', 25, 30, '2025-12-18 13:00:00', '2025-12-18 13:00:00'),
(5, 'NEW_YEAR_SALE', 30, 365, '2026-01-01 00:00:00', '2026-01-01 00:00:00');

-- member_id=1인 회원이 사용한 1개의 쿠폰과 미사용한 쿠폰 1개를 소유
INSERT INTO coupon (id, coupon_summary_id, member_id, using_at, expired_at, created_at, updated_at) VALUES
(1, 1, 1, '2025-03-20 13:00:00', '2025-03-21 13:00:00', '2025-03-18 13:00:00', '2025-03-20 13:00:00'),
(2, 4, 1, NULL, '2026-01-18 13:00:00', '2025-12-18 13:00:00', '2025-12-18 13:00:00');
