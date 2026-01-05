-- 쿠폰 동시성 테스트 데이터 정리

-- 결제 및 주문 데이터 삭제 (TC-CPN-003 용)
DELETE FROM payment_summary WHERE order_summary_id IN (
    SELECT id FROM order_summary WHERE member_id = 5004
);
DELETE FROM order_item WHERE order_summary_id IN (
    SELECT id FROM order_summary WHERE member_id = 5004
);
DELETE FROM order_summary WHERE member_id = 5004;

-- 상품 데이터 삭제 (TC-CPN-003 용)
DELETE FROM product_summary WHERE id = 6001;

-- 발급된 쿠폰 삭제
DELETE FROM coupon WHERE member_id IN (5001, 5002, 5003, 5004);

-- 쿠폰 발급 정책 삭제
DELETE FROM coupon_issuance_policy WHERE coupon_issuance_id IN (6001, 6002, 6003);

-- 쿠폰 발급 정보 삭제
DELETE FROM coupon_issuance WHERE id IN (6001, 6002, 6003);

-- 쿠폰 정보 삭제
DELETE FROM coupon_summary WHERE id IN (6001, 6002, 6003);

-- 잔액 및 회원 데이터 삭제
DELETE FROM member_balance WHERE id IN (5001, 5002, 5003, 5004);
DELETE FROM member WHERE id IN (5001, 5002, 5003, 5004);
