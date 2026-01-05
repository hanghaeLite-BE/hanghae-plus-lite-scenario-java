-- 잔액 동시성 테스트 데이터 정리

-- TC-BAL-002: 주문 관련 데이터 삭제
DELETE FROM payment_summary WHERE order_summary_id IN (
    SELECT id FROM order_summary WHERE member_id = 3002
);
DELETE FROM order_item WHERE order_summary_id IN (
    SELECT id FROM order_summary WHERE member_id = 3002
);
DELETE FROM order_summary WHERE member_id = 3002;

-- TC-BAL-002: 상품 데이터 삭제
DELETE FROM product_summary WHERE id = 3001;

-- 잔액 및 회원 데이터 삭제
DELETE FROM member_balance WHERE id IN (3001, 3002);
DELETE FROM member WHERE id IN (3001, 3002);
