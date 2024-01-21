INSERT INTO app_user (id, firstname, lastname, email, password, role, locked, enabled)
VALUES (1000, 'John', 'Doe', 'john.doe@example.com', '$2a$10$bITm.1PZYpJXip2RijlP3OfTWiE0NRK0.BhgJAf2ivnZFfSKnSQuW',
        'ROLE_USER', false, true),
       (1001, 'Jane', 'Smith', 'jane.smith@example.com', '$2a$10$bITm.1PZYpJXip2RijlP3OfTWiE0NRK0.BhgJAf2ivnZFfSKnSQuW',
        'ROLE_ADMIN', false, true),
       (1002, 'Alice', 'Johnson', 'alice.johnson@example.com',
        '$2a$10$bITm.1PZYpJXip2RijlP3OfTWiE0NRK0.BhgJAf2ivnZFfSKnSQuW',
        'ROLE_USER', false, false),
       (1003, 'Mark', 'Anderson', 'mark.anderson@example.com', '$2a$10$bITm.1PZYpJXip2RijlP3OfTWiE0NRK0.BhgJAf2ivnZFfSKnSQuW',
        'ROLE_USER', false, true);

INSERT INTO authentication_token(id, token, token_type, revoked, expired, user_id)
VALUES (1000,
        'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NjQyNjc0OSwiZXhwIjoxMDMzNjQyNjc0OX0.0pl9Ee9SmSZbOv1AVeXbQuwkcW1l8TUpEBhZrv0EwDo',
        'ACCESS', false, false, 1000),
       (1001,
        'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NjQyNjgyMSwiZXhwIjoxMDMzNjQyNjgyMX0.jeeuTzGgKrixMp6_dizMNLicp6n0gwECAId-ATLqbns',
        'REFRESH', false, false, 1000),
       (1002,
        'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZS5qb2huc29uQGV4YW1wbGUuY29tIiwiaWF0IjoxNjk2NDI2ODIyLCJleHAiOjEwMzM2NDI2ODIyfQ.K8ACIiQKVcSr5IX_snOX-WXNebx2-FMIQP4gj4Qg_Pk',
        'REFRESH', false, false, 1001);

INSERT INTO confirmation_token(id, token, token_type, created_at, expires_at, confirmed_at, user_id)
VALUES (1000, '8e5648d7-9b4e-4724-83a1-be7e64603e48', 'ACTIVATION', '2023-09-22 15:47:15.593933',
        '2226-09-22 16:02:15.593933', null, 1000),
       (1001, '8e5648d7-9b4e-4724-83a1-be7e64603e47', 'RESET_PASSWORD', '2023-09-22 15:47:15.593933',
        '2226-09-22 16:02:15.593933', null, 1001);

INSERT INTO age_category(id, name, slug)
VALUES (1, 'до 1 року', 'do-1-roku'),
       (2, 'від 1 до 3 років', 'vid-1-do-3-rokiv'),
       (3, 'від 6 до 9 років', 'vid-6-do-9-rokiv');

INSERT INTO brand_category(id, name, slug)
VALUES (1, 'CoComelon', 'сoсomelon'),
       (2, 'Сurlimals', 'сurlimals'),
       (3, 'Devilon', 'devilon'),
       (4, 'Disney', 'disney');

INSERT INTO origin_category(id, name, slug)
VALUES (1, 'Китай', 'china'),
       (2, 'Україна', 'ukraine');

INSERT INTO product(id, name, slug, description, price, available_quantity, origin_id, brand_id)
VALUES (1, 'Лялька Клаймбер', 'lyalka-klaymber',
        'Ця іграшка об''єднує інноваційний дизайн та розвиваючий функціонал, що сприяє розвитку навичок у дітей. Вона створює захоплюючий світ уяви, розвиваючи логічне мислення та творчість. Іграшка безпечна, енергоефективна і сприяє розвитку спостережливості, уваги та винахідливості у дітей, забезпечуючи незабутні враження та навчальний досвід.',
        850, 1, 1, 1),
       (2, 'Лялька Даринка', 'lyalka-darynka',
        'Ця іграшка об''єднує інноваційний дизайн та розвиваючий функціонал, що сприяє розвитку навичок у дітей. Вона створює захоплюючий світ уяви, розвиваючи логічне мислення та творчість. Іграшка безпечна, енергоефективна і сприяє розвитку спостережливості, уваги та винахідливості у дітей, забезпечуючи незабутні враження та навчальний досвід.',
        900, 200, 2, 2),
       (3, 'Лялька Русалочка', 'lyalka-rusalochka',
        'Ця іграшка об''єднує інноваційний дизайн та розвиваючий функціонал, що сприяє розвитку навичок у дітей. Вона створює захоплюючий світ уяви, розвиваючи логічне мислення та творчість. Іграшка безпечна, енергоефективна і сприяє розвитку спостережливості, уваги та винахідливості у дітей, забезпечуючи незабутні враження та навчальний досвід.',
        550, 0, 1, 3),
       (4, 'Пупсик Оксанка', 'pupsik_oksanka',
        'Ця іграшка об''єднує інноваційний дизайн та розвиваючий функціонал, що сприяє розвитку навичок у дітей. Вона створює захоплюючий світ уяви, розвиваючи логічне мислення та творчість. Іграшка безпечна, енергоефективна і сприяє розвитку спостережливості, уваги та винахідливості у дітей, забезпечуючи незабутні враження та навчальний досвід.',
        500, 150, 1, 4);

INSERT INTO product_age_category(product_id, age_category_id)
VALUES (1, 2),
       (1, 3),
       (2, 2),
       (2, 3),
       (3, 2);

INSERT INTO image(id, name, type, image, product_id)
VALUES (1, 'lyalka-klaymber1.png', 'image/png', X'1F8B', 1);

UPDATE product
SET image_id = 1
WHERE id = 1;

INSERT INTO cart_item(user_id, product_id, quantity)
VALUES (1000, 2, 1),
       (1000, 4, 4),
       (1003, 1, 4),
       (1003, 3, 4);

INSERT INTO address(id, region, settlement, street, house, apartment)
VALUES (1000, 'Київська обл.', 'м. Київ', 'вул. Івана Франка', 12, 158);

INSERT INTO phone_number(id, country_code, operator_code, number)
VALUES (1000, 'UA', '95', '1234567');

INSERT INTO order_recipient(id, firstname, patronymic, lastname, address_id, phone_id)
VALUES (1000, 'Іван', 'Іванович', 'Франко', 1000, 1000);

INSERT INTO app_order(id, date_time, order_status, recipient_id, total_price, user_id)
VALUES ('4c980930-16eb-41cd-b998-29d03118d67c', '2023-11-19 18:09:52.242927', 'AWAITING_PAYMENT', 1000, 1000, 1000);

INSERT INTO order_details(order_id, product_id, quantity)
VALUES ('4c980930-16eb-41cd-b998-29d03118d67c', 1, 2);