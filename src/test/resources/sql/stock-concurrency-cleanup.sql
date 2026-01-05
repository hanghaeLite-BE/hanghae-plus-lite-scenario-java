-- 재고 동시성 테스트 데이터 정리

-- 결제 및 주문 데이터 삭제
DELETE FROM payment_summary WHERE order_summary_id IN (
    SELECT id FROM order_summary WHERE member_id IN (4001, 4002, 4003, 4004, 4005, 4006, 4007)
);
DELETE FROM order_item WHERE order_summary_id IN (
    SELECT id FROM order_summary WHERE member_id IN (4001, 4002, 4003, 4004, 4005, 4006, 4007)
);
DELETE FROM order_summary WHERE member_id IN (4001, 4002, 4003, 4004, 4005, 4006, 4007);

-- 상품 데이터 삭제
DELETE FROM product_summary WHERE id IN (5001, 5002);

-- 잔액 및 회원 데이터 삭제
DELETE FROM member_balance WHERE id IN (4001, 4002, 4003, 4004, 4005, 4006, 4007);
DELETE FROM member WHERE id IN (4001, 4002, 4003, 4004, 4005, 4006, 4007);
