-- 분산락 동시성 테스트 데이터 정리

-- 주문 아이템 삭제
DELETE FROM order_item WHERE order_summary_id IN (
    SELECT id FROM order_summary WHERE member_id BETWEEN 5001 AND 5100 OR member_id BETWEEN 5201 AND 5220
);

-- 주문 요약 삭제
DELETE FROM order_summary WHERE member_id BETWEEN 5001 AND 5100 OR member_id BETWEEN 5201 AND 5220;

-- 회원 잔액 삭제
DELETE FROM member_balance WHERE member_id BETWEEN 5001 AND 5100 OR member_id BETWEEN 5201 AND 5220;

-- 회원 삭제
DELETE FROM member WHERE id BETWEEN 5001 AND 5100 OR id BETWEEN 5201 AND 5220;

-- 상품 삭제
DELETE FROM product_summary WHERE id IN (6001, 6002);
