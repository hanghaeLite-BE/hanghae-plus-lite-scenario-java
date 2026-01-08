-- 회원 정보 (사용자 100명)
INSERT INTO member (id, name, point, created_at, updated_at) 
SELECT seq, CONCAT('User', seq), 100000, NOW(), NOW()
FROM (SELECT @row := @row + 1 AS seq FROM (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1, (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2, (SELECT @row := 0) r) t 
LIMIT 100;

-- 콘서트 정보
INSERT INTO concert (id, title, created_at, updated_at) VALUES (1, 'Test Concert', NOW(), NOW());

-- 콘서트 일정 정보 (회차)
INSERT INTO concert_schedule (id, concert_id, concert_date, reservation_start_at, reservation_end_at, created_at, updated_at) 
VALUES (1, 1, DATE_ADD(NOW(), INTERVAL 7 DAY), NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), NOW(), NOW());

-- 좌석 정보 (1번 좌석 1개, 11~30번 좌석 20개)
INSERT INTO seat (id, schedule_id, seat_number, price, status, created_at, updated_at) VALUES (1, 1, 1, 10000, 'AVAILABLE', NOW(), NOW());

INSERT INTO seat (id, schedule_id, seat_number, price, status, created_at, updated_at) 
SELECT seq + 10, 1, seq + 10, 10000, 'AVAILABLE', NOW(), NOW()
FROM (SELECT @row2 := @row2 + 1 AS seq FROM (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1, (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2, (SELECT @row2 := 0) r) t 
LIMIT 20;

-- 대기열 토큰 (100명분에 대해 유효한 토큰 생성)
INSERT INTO reservation_token (id, user_id, token, status, expires_at, created_at, updated_at)
SELECT seq, seq, CONCAT('token-', seq), 'WAITING', DATE_ADD(NOW(), INTERVAL 1 HOUR), NOW(), NOW()
FROM (SELECT @row3 := @row3 + 1 AS seq FROM (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1, (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2, (SELECT @row3 := 0) r) t 
LIMIT 100;

-- 토큰 상태를 ACTIVE로 변경 (테스트 편의상)
UPDATE reservation_token SET status = 'ACTIVE' WHERE user_id <= 100;
