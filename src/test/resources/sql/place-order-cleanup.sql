-- 회원 데이터 삭제
DELETE FROM member WHERE id = 1001;

-- 회원의 잔액 데이터 삭제
DELETE FROM member_balance WHERE id = 1001;

-- 쿠폰 데이터 삭제
DELETE FROM coupon WHERE id = 1001;

-- 쿠폰 발급 및 발급 정책 데이터 삭제
DELETE FROM coupon_issuance WHERE id = 1001;
DELETE FROM coupon_issuance_policy WHERE id = 1001;

-- 쿠폰정보 데이터 삭제
DELETE FROM coupon_summary WHERE id = 1001;

-- 상품 데이터 삭제
DELETE FROM product_summary WHERE id IN (1001, 1002, 1003);

-- 주문정보 및 결제정보 삭제
DELETE FROM order_item WHERE (
    SELECT id FROM order_summary WHERE member_id = 1001
);

DELETE FROM payment_summary WHERE order_summary_id IN (
    SELECT id FROM order_summary WHERE member_id = 1001
);

DELETE FROM order_summary WHERE member_id = 1001;
