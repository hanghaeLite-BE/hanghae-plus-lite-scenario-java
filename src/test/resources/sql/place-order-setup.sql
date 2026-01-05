-- 회원 데이터 생성 (1000번대)
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at)
VALUES (1001, 'njz@gmail.com', 'securepassword', 'GENERAL', '2025-01-02 00:00:00', '2025-01-18 13:20:12');

-- 회원의 잔액 데이터 생성
INSERT INTO member_balance (id, member_id, balance, created_at, updated_at)
VALUES (1001, 1001, 2000000, '2025-01-02 00:00:00', '2025-03-18 13:00:00');

-- 쿠폰정보 데이터 생성
INSERT INTO coupon_summary (id, name, discount_percentage, valid_days, created_at, updated_at)
VALUES (1001, '10% 할인 쿠폰', 10, 90, '2025-03-18 13:00:00', '2025-03-18 13:00:00');

CREATE TABLE IF NOT EXISTS coupon_issuance (
    id BIGINT NOT NULL AUTO_INCREMENT,
    coupon_summary_id BIGINT NOT NULL,
    issued_count INT NOT NULL,
    max_count INT NULL COMMENT '최대 발급 수 (NULL이면 무제한)',
    start_at DATETIME(6) NOT NULL,
    end_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 쿠폰 발급 데이터 생성
INSERT INTO coupon_issuance (id, coupon_summary_id, issued_count, max_count, start_at, end_at, created_at, updated_at)
VALUES (1001, 1001, 1, 10, '2025-03-18 13:00:00', '2025-12-31 23:59:59', '2025-03-18 13:00:00', '2025-03-18 13:00:00');

-- 쿠폰 발급 정책 데이터 생성
INSERT INTO coupon_issuance_policy (id, coupon_issuance_id, policy_type, created_at, updated_at)
VALUES (1001, 1001, 'ONE_PER_MEMBER', '2025-03-18 13:00:00', '2025-03-18 13:00:00');

-- member_id=1001인 회원이 쿠폰을 소유한 상태
INSERT INTO coupon (id, coupon_summary_id, member_id, using_at, expired_at, created_at, updated_at)
VALUES (1001, 1001, 1001, NULL, '2025-06-18 13:00:00', '2025-03-18 13:00:00', '2025-03-20 13:00:00');

-- 상품 데이터 생성
INSERT INTO product_summary (id, name, price, stock_quantity, created_at, updated_at)
VALUES (1001, '아이폰 17 pro', 1790000, 10, '2025-03-18 13:00:00', '2025-03-18 13:00:00'),
       (1002, '케스티파이 케이스', 89000, 20, '2025-03-18 13:00:00', '2025-03-18 13:00:00'),
       (1003, '신지모루 액정필름', 12000, 100, '2025-03-18 13:00:00', '2025-03-18 13:00:00');


