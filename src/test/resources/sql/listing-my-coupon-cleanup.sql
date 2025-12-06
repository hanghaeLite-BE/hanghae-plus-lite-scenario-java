-- 쿠폰정보 삭제
DELETE FROM coupon_summary WHERE id IN (1, 2, 3, 4, 5);

-- 쿠폰 데이터 삭제
DELETE FROM coupon WHERE id IN (1, 2);