DELETE FROM coupon WHERE coupon_summary_id = 100;

DELETE FROM coupon_issuance_policy WHERE id = 100;

DELETE FROM coupon_issuance WHERE id = 100;

DELETE FROM coupon_summary WHERE id = 100;

DELETE FROM member WHERE id IN (101, 102);
