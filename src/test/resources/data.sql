INSERT INTO app_user (id, firstname, lastname, email, password, role, locked, enabled)
VALUES (1000, 'John', 'Doe', 'john.doe@example.com', '$2a$10$WKoHCVQBee77aRXkS.xpceIyBqRcjlkjfpYCerq8eZJkf6Uvm8QPq',
        'ROLE_USER', false, true),
       (1001, 'Jane', 'Smith', 'jane.smith@example.com', '$2a$10$WKoHCVQBee77aRXkS.xpceIyBqRcjlkjfpYCerq8eZJkf6Uvm8QPq',
        'ROLE_ADMIN', false, true),
       (1002, 'Alice', 'Johnson', 'alice.johnson@example.com', '$2a$10$WKoHCVQBee77aRXkS.xpceIyBqRcjlkjfpYCerq8eZJkf6Uvm8QPq',
        'ROLE_USER', false, false);

INSERT INTO authentication_token(id, token, token_type, revoked, expired, user_id)
VALUES (1000, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NjQyNjc0OSwiZXhwIjoxMDMzNjQyNjc0OX0.0pl9Ee9SmSZbOv1AVeXbQuwkcW1l8TUpEBhZrv0EwDo',
        'ACCESS', false, false, 1000),
       (1001, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NjQyNjgyMSwiZXhwIjoxMDMzNjQyNjgyMX0.jeeuTzGgKrixMp6_dizMNLicp6n0gwECAId-ATLqbns',
        'REFRESH', false, false, 1000),
       (1002, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZS5qb2huc29uQGV4YW1wbGUuY29tIiwiaWF0IjoxNjk2NDI2ODIyLCJleHAiOjEwMzM2NDI2ODIyfQ.K8ACIiQKVcSr5IX_snOX-WXNebx2-FMIQP4gj4Qg_Pk',
        'REFRESH', false, false, 1002);

INSERT INTO confirmation_token(id, token, token_type, created_at, expires_at, confirmed_at, user_id)
VALUES (1000, '8e5648d7-9b4e-4724-83a1-be7e64603e48', 'ACTIVATION', '2023-09-22 15:47:15.593933',
        '2226-09-22 16:02:15.593933', null, 1000),
       (1001, '8e5648d7-9b4e-4724-83a1-be7e64603e47', 'RESET_PASSWORD', '2023-09-22 15:47:15.593933',
        '2226-09-22 16:02:15.593933', null, 1000);