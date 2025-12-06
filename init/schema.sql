-- 0. 회원 테이블
CREATE TABLE IF NOT EXISTS member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    pwd VARCHAR(255) NOT NULL,
    member_type VARCHAR(50) NOT NULL DEFAULT 'GENERAL' COMMENT '회원 유형. [GENERAL, PREMIUM, ADMIN]',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 이메일 유니크 인덱스
CREATE UNIQUE INDEX idx_member_email ON member(email);

-- 1. 회원 잔액 테이블
CREATE TABLE IF NOT EXISTS member_balance (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    balance BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 회원별 유니크 인덱스 (한 회원당 하나의 잔액 레코드)
CREATE UNIQUE INDEX idx_member_balance_member_id ON member_balance(member_id);

-- 2. 상품 정보 테이블
CREATE TABLE IF NOT EXISTS product_summary (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    price INT NOT NULL,
    stock_quantity INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_product_summary_name ON product_summary(name);

-- 3. 쿠폰정보 테이블
CREATE TABLE IF NOT EXISTS coupon_summary (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    discount_percentage BIGINT NOT NULL,
    valid_days INT NULL COMMENT '발급 후 유효기간(일)',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 쿠폰 테이블
CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT NOT NULL AUTO_INCREMENT,
    coupon_summary_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    expired_at DATETIME(6) NULL,
    using_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_coupon_member_id ON coupon(member_id);
CREATE INDEX idx_coupon_coupon_summary_id ON coupon(coupon_summary_id);

-- 5. 쿠폰 발급 상태 테이블
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
CREATE INDEX idx_coupon_issuance_coupon_summary_id ON coupon_issuance(coupon_summary_id);
CREATE INDEX idx_coupon_issuance_period ON coupon_issuance(start_at, end_at);

-- 6. 쿠폰발급정책 테이블 (SINGLE_TABLE, policy_type 컬럼)
CREATE TABLE IF NOT EXISTS coupon_issuance_policy (
    id BIGINT NOT NULL AUTO_INCREMENT,
    coupon_issuance_id BIGINT NOT NULL,
    policy_type VARCHAR(50) NOT NULL COMMENT '정책 유형. [ONE_PER_MEMBER, ...]',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_coupon_issuance_policy_coupon_issuance_id ON coupon_issuance_policy(coupon_issuance_id);

-- 7. 주문 요약 테이블
CREATE TABLE IF NOT EXISTS order_summary (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_order_summary_member_id ON order_summary(member_id);

-- 8. 주문 상세 테이블
CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_summary_id BIGINT NOT NULL,
    product_summary_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_order_item_order_summary_id ON order_item(order_summary_id);
CREATE INDEX idx_order_item_product_summary_id ON order_item(created_at, product_summary_id);

-- 9. 결제 요약 테이블
CREATE TABLE IF NOT EXISTS payment_summary (
    id BIGINT NOT NULL AUTO_INCREMENT,
    method VARCHAR(50) NOT NULL,
    total_amount BIGINT NOT NULL,
    discount_amount BIGINT NOT NULL,
    charge_amount BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    order_summary_id BIGINT NOT NULL,
    coupon_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_payment_summary_member_id ON payment_summary(member_id);
CREATE INDEX idx_payment_summary_order_summary_id ON payment_summary(order_summary_id);
CREATE INDEX idx_payment_summary_coupon_id ON payment_summary(coupon_id);
