-- 통합테스트에서 사용할 공통 데이터 초기화
-- member 테이블에 샘플 데이터 삽입
INSERT INTO member (id, email, pwd, member_type, created_at, updated_at)
VALUES (1, 'alicejohnson@gmail.com', '1234', 'GENERAL', '2024-10-01 10:00:00', '2024-10-01 10:00:00'),
       (2, 'bobsmith@naver.com', '1234', 'GENERAL', '2024-10-02 11:30:00', '2024-10-02 11:30:00'),
       (3, 'charliebrown@kakao.com', '1234', 'GENERAL', '2024-10-03 09:15:00', '2024-10-03 09:15:00');

-- member_balance 테이블에 샘플 데이터 삽입
INSERT INTO member_balance (id, member_id, balance, created_at, updated_at)
VALUES (1, 1, 10000, '2024-10-01 10:00:00', '2024-10-01 10:00:00'),
       (2, 2, 25000, '2024-10-02 11:30:00', '2024-10-02 11:30:00'),
       (3, 3, 15000, '2024-10-03 09:15:00', '2024-10-03 09:15:00');
