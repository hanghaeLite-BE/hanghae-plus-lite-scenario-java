-- 잔액 동시성 테스트용 데이터

-- TC-BAL-001: 동시 충전 테스트용 회원 (초기 잔액 10,000원)
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at)
VALUES (3001, 'balance-recharge-test@example.com', 'password123', 'GENERAL', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO member_balance (id, member_id, balance, created_at, updated_at)
VALUES (3001, 3001, 10000, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

-- TC-BAL-002: 동시 주문 시 잔액 차감 테스트용 회원 (초기 잔액 15,000원)
-- 시나리오: 동일 사용자가 동시에 2건의 주문 (각 10,000원) 요청
-- 기대 결과: 1건 성공, 1건 실패 (잔액 부족)
-- 동시성 이슈 발생 시: 2건 모두 성공, 잔액 음수
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at)
VALUES (3002, 'balance-order-deduction-test@example.com', 'password123', 'GENERAL', '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO member_balance (id, member_id, balance, created_at, updated_at)
VALUES (3002, 3002, 15000, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

-- TC-BAL-002: 주문용 상품 (충분한 재고 보유 - 잔액이 제한 요소가 되도록)
INSERT INTO product_summary (id, name, price, stock_quantity, created_at, updated_at)
VALUES (3001, '잔액 테스트용 상품', 10000, 100, '2025-01-01 00:00:00', '2025-01-01 00:00:00');
