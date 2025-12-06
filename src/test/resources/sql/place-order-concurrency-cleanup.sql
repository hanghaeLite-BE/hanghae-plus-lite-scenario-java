-- 테스트에서 생성된 결제 및 주문 데이터 삭제
DELETE FROM payment_summary WHERE order_summary_id IN (SELECT id FROM order_summary WHERE member_id = 2004);
DELETE FROM order_item WHERE order_summary_id IN (SELECT id FROM order_summary WHERE member_id = 2004);
DELETE FROM order_summary WHERE member_id = 2004;

-- 쿠폰 데이터 삭제
DELETE FROM coupon WHERE id = 2000;
DELETE FROM coupon_summary WHERE id = 200;

-- 회원 잔액 및 상품 데이터 삭제
DELETE FROM member_balance WHERE id = 2004;
DELETE FROM product_summary WHERE id IN (9001, 9002);

-- 회원 데이터 삭제
DELETE FROM member WHERE id = 2004;
