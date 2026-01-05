DELETE FROM coupon_summary WHERE id IN (7001, 7002, 7003);

DELETE FROM coupon_issuance WHERE id IN (7001, 7002, 7003);

DELETE FROM coupon_issuance_policy WHERE id = 7001;

DELETE FROM coupon WHERE id = 7001;
