INSERT INTO app_user (id, firstname, lastname, email, password, role, locked, enabled)
VALUES (1000, 'John', 'Doe', 'john.doe@example.com', '$2a$10$WKoHCVQBee77aRXkS.xpceIyBqRcjlkjfpYCerq8eZJkf6Uvm8QPq',
        'ROLE_USER', false, true),
       (1001, 'Jane', 'Smith', 'jane.smith@example.com', '$2a$10$WKoHCVQBee77aRXkS.xpceIyBqRcjlkjfpYCerq8eZJkf6Uvm8QPq',
        'ROLE_ADMIN', false, true),
       (1002, 'Alice', 'Johnson', 'alice.johnson@example.com', '$2a$10$WKoHCVQBee77aRXkS.xpceIyBqRcjlkjfpYCerq8eZJkf6Uvm8QPq',
        'ROLE_USER', false, false);

INSERT INTO authentication_token(id, token, token_type, revoked, expired, user_id)
VALUES (1000, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NTM3NjA3NiwiZXhwIjoxNzk1Mzc3NTE2fQ.7z-SJjVtAamFjOo0qLd0ehtO59ODHw2B7j1dm4nynE4',
        'ACCESS', false, false, 1000),
       (1001, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NTM3NjA3NiwiZXhwIjoxNzk1Mzk2MjM2fQ.7DMHH20DBuaonWaeua9JpY6UUL5KtiRjeP7ZpoZrQ-U',
        'REFRESH', false, false, 1000);

INSERT INTO confirmation_token(id, token, token_type, created_at, expires_at, confirmed_at, user_id)
VALUES (1000, '8e5648d7-9b4e-4724-83a1-be7e64603e48', 'ACTIVATION', '2023-09-22 15:47:15.593933',
        '2226-09-22 16:02:15.593933', null, 1000),
       (1001, '8e5648d7-9b4e-4724-83a1-be7e64603e47', 'RESET_PASSWORD', '2023-09-22 15:47:15.593933',
        '2226-09-22 16:02:15.593933', null, 1000);