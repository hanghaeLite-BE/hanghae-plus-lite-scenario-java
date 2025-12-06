DELETE FROM coupon_summary WHERE id IN (1, 2, 3);

DELETE FROM coupon_issuance WHERE id IN (1, 2, 3);

DELETE FROM coupon_issuance_policy WHERE id = 1;

DELETE FROM coupon WHERE id = 1;
