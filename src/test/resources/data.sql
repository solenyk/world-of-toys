INSERT INTO app_user (id, firstname, lastname, email, password, role, locked, enabled)
VALUES (1, 'John', 'Doe', 'john.doe@example.com', '$2a$10$8Cq9HDEsVwbd4zaHEJiKseCVOMsJQRfzIeIN8v6KoBiC9H2XMcJUm',
        'ROLE_USER', false, true),
       (2, 'Jane', 'Smith', 'jane.smith@example.com', '$2a$10$8Cq9HDEsVwbd4zaHEJiKseCVOMsJQRfzIeIN8v6KoBiC9H2XMcJUm',
        'ROLE_ADMIN', false, true);

INSERT INTO authentication_token(id, token, token_type, revoked, expired, user_id)
VALUES (1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NTM3NjA3NiwiZXhwIjoxNzk1Mzc3NTE2fQ.7z-SJjVtAamFjOo0qLd0ehtO59ODHw2B7j1dm4nynE4',
        'ACCESS', false, false, 1),
       (2, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NTM3NjA3NiwiZXhwIjoxNzk1Mzk2MjM2fQ.7DMHH20DBuaonWaeua9JpY6UUL5KtiRjeP7ZpoZrQ-U',
        'REFRESH', false, false, 1);

INSERT INTO confirmation_token(id, token, token_type, created_at, expires_at, confirmed_at, user_id)
VALUES (1, '8e5648d7-9b4e-4724-83a1-be7e64603e48', 'ACTIVATION', '2023-09-22 15:47:15.593933',
        '2023-09-22 16:02:15.593933', null, 1),
       (2, '8e5648d7-9b4e-4724-83a1-be7e64603e47', 'RESET_PASSWORD', '2023-09-22 15:47:15.593933',
        '2023-09-22 16:02:15.593933', null, 1);