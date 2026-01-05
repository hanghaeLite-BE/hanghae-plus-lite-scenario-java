-- 쿠폰정보 삭제
DELETE FROM coupon_summary WHERE id IN (8001, 8002, 8003, 8004, 8005);

-- 쿠폰 데이터 삭제
DELETE FROM coupon WHERE id IN (8001, 8002);
