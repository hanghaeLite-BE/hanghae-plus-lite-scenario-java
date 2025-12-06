-- 회원 데이터 삭제
DELETE FROM member WHERE id = 4;

-- 회원의 잔액 데이터 삭제
DELETE FROM member_balance WHERE id = 4;

-- 쿠폰 데이터 삭제
DELETE FROM coupon WHERE id = 1;

-- 쿠폰정보 데이터 삭제
DELETE FROM coupon_summary WHERE id = 1;

-- 상품 데이터 삭제
DELETE FROM product_summary WHERE id IN (1, 2, 3);
